package com.lifelink.request;

import com.lifelink.bloodchain.BloodChainService;
import com.lifelink.donor.Donor;
import com.lifelink.donor.DonorRepository;
import com.lifelink.matching.MatchingEngine;
import com.lifelink.notification.FcmService;
import com.lifelink.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;
    private final RequestResponseRepository requestResponseRepository;
    private final DonorRepository donorRepository;
    private final UserRepository userRepository;
    private final MatchingEngine matchingEngine;
    private final FcmService fcmService;
    private final BloodChainService bloodChainService;
    private final RequestEventRepository requestEventRepository;
    private final com.lifelink.webhook.WebhookService webhookService;
    private final com.lifelink.institution.BloodBankRepository bloodBankRepository;
    private final com.lifelink.institution.HospitalForwardRepository hospitalForwardRepository;

    @Transactional
    public EmergencyRequest createRequest(EmergencyRequest request) {
        request.setStatus(RequestStatus.PENDING);
        request.setCurrentRadiusKm(5);
        if (request.getExpiresAt() == null) {
            request.setExpiresAt(LocalDateTime.now().plusHours(24));
        }
        EmergencyRequest saved = requestRepository.save(request);
        logEventAndNotify(saved, "CREATED", "Emergency request created for blood type " + request.getBloodType());
        broadcastToDonors(saved);
        return saved;
    }

    @Transactional
    public void fulfillRequest(UUID requestId) {
        EmergencyRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        if (request.getStatus() != RequestStatus.PENDING && request.getStatus() != RequestStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot fulfill request in state: " + request.getStatus());
        }

        request.setStatus(RequestStatus.FULFILLED);
        requestRepository.save(request);
        logEventAndNotify(request, "STATUS_CHANGED", "Emergency request status changed to FULFILLED");

        // Notify other donors to stand down
        List<RequestResponse> responses = requestResponseRepository.findByRequestId(requestId);
        responses.stream()
                .filter(r -> r.getStatus() == RequestResponseStatus.NOTIFIED || r.getStatus() == RequestResponseStatus.ACCEPTED)
                .map(RequestResponse::getDonor)
                .forEach(d -> fcmService.sendStandDownNotification(d.getFcmToken(), requestId.toString()));
    }

    @Transactional
    public void cancelRequest(UUID requestId) {
        EmergencyRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        if (request.getStatus() != RequestStatus.PENDING && request.getStatus() != RequestStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot cancel request in state: " + request.getStatus());
        }

        request.setStatus(RequestStatus.CANCELLED);
        requestRepository.save(request);
        logEventAndNotify(request, "STATUS_CHANGED", "Emergency request status changed to CANCELLED");
    }

    /**
     * Handles a donor's explicit ACCEPT or DECLINE response to a blood request notification.
     *
     * - ACCEPTED: marks the response, promotes request to IN_PROGRESS on first acceptance.
     * - DECLINED:  marks the response with a timestamp. Donor is excluded from future broadcasts for this request.
     *
     * @param requestId the request being responded to
     * @param donorPhone the authenticated donor's phone number
     * @param newStatus  must be ACCEPTED or DECLINED
     */
    @Transactional
    public void respondToRequest(UUID requestId, String donorPhone, RequestResponseStatus newStatus) {
        if (newStatus != RequestResponseStatus.ACCEPTED && newStatus != RequestResponseStatus.DECLINED) {
            throw new IllegalArgumentException("Donors may only respond with ACCEPTED or DECLINED");
        }

        EmergencyRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + requestId));

        if (request.getStatus() == RequestStatus.FULFILLED
                || request.getStatus() == RequestStatus.CANCELLED
                || request.getStatus() == RequestStatus.EXPIRED) {
            throw new IllegalStateException("Cannot respond to a request in state: " + request.getStatus());
        }

        Donor donor = donorRepository.findByUserId(
                        userRepository.findByPhone(donorPhone)
                                .orElseThrow(() -> new IllegalArgumentException("User not found")).getId())
                .orElseThrow(() -> new IllegalArgumentException("Donor profile not found"));

        RequestResponse response = requestResponseRepository
                .findByRequestIdAndDonorId(requestId, donor.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Donor was not notified about this request — cannot respond"));

        if (response.getStatus() == RequestResponseStatus.ACCEPTED
                || response.getStatus() == RequestResponseStatus.DECLINED) {
            throw new IllegalStateException("Donor has already responded to this request");
        }

        response.setStatus(newStatus);
        response.setRespondedAt(LocalDateTime.now());
        requestResponseRepository.save(response);

        logResponseEventAndNotify(request, donor, newStatus, "Donor responded: " + newStatus);

        if (newStatus == RequestResponseStatus.ACCEPTED
                && request.getStatus() == RequestStatus.PENDING) {
            request.setStatus(RequestStatus.IN_PROGRESS);
            requestRepository.save(request);
            logEventAndNotify(request, "STATUS_CHANGED", "Emergency request status changed to IN_PROGRESS");
            log.info("Request {} moved to IN_PROGRESS after donor {} accepted", requestId, donor.getId());
        }
    }

    /**
     * Returns a single emergency request by ID, for use in controllers.
     */
    @Transactional(readOnly = true)
    public EmergencyRequest getById(UUID requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + requestId));
    }

    private void broadcastToDonors(EmergencyRequest request) {
        List<Donor> eligibleDonors = matchingEngine.findEligibleDonors(request, request.getCurrentRadiusKm());

        // Filter out already notified donors
        List<Donor> unnotifiedDonors = eligibleDonors.stream()
                .filter(donor -> requestResponseRepository.findByRequestIdAndDonorId(request.getId(), donor.getId()).isEmpty())
                .collect(java.util.stream.Collectors.toList());

        if (unnotifiedDonors.isEmpty() && request.getCurrentRadiusKm() >= maxRadiusKm) {
            // ----------------------------------------------------------------
            // BLOOD CHAIN ACTIVATION: No eligible donors found within max radius.
            // Trigger SMS invites to trusted contacts of all active donors.
            // ----------------------------------------------------------------
            log.warn("No donors found at max radius {}km for request {}. Activating SOS and Blood Chain.",
                    maxRadiusKm, request.getId());
            bloodChainService.activateBloodChain(request);
            triggerBloodBankFallback(request);
            return;
        }

        List<Donor> batchToNotify;
        if (request.isDisasterMode()) {
            batchToNotify = unnotifiedDonors; // Broadcast to all immediately
        } else {
            batchToNotify = unnotifiedDonors.stream().limit(5).collect(java.util.stream.Collectors.toList());
        }

        for (Donor donor : batchToNotify) {
            com.lifelink.request.RequestResponse response = new com.lifelink.request.RequestResponse();
            response.setRequest(request);
            response.setDonor(donor);
            response.setStatus(com.lifelink.request.RequestResponseStatus.NOTIFIED);
            requestResponseRepository.save(response);

            fcmService.sendNotificationToDonor(donor, "Emergency Blood Request",
                    "Blood type " + request.getBloodType() + " is needed urgently.",
                    request.getUrgency() == com.lifelink.request.Urgency.CRITICAL,
                    request.getId().toString());
        }
    }

    private void triggerBloodBankFallback(EmergencyRequest request) {
        List<com.lifelink.institution.BloodBank> nearbyBanks = bloodBankRepository.findActiveBloodBanksWithinRadius(
                request.getLocation(), maxRadiusKm * 1000.0);
        for (com.lifelink.institution.BloodBank bank : nearbyBanks) {
            com.lifelink.institution.HospitalForward forward = new com.lifelink.institution.HospitalForward();
            forward.setRequest(request);
            forward.setBloodBank(bank);
            forward.setStatus("PENDING");
            hospitalForwardRepository.save(forward);
            log.info("Forwarded request {} to blood bank {}", request.getId(), bank.getName());
        }
    }

    @org.springframework.beans.factory.annotation.Value("${matching.max-radius-km:50}")
    private int maxRadiusKm;

    // Auto-expand radius every 5 minutes if no response
    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void autoExpandRadius() {
        List<EmergencyRequest> activeRequests = requestRepository.findByStatus(RequestStatus.PENDING);
        for (EmergencyRequest req : activeRequests) {
            if (req.isDisasterMode()) {
                // In disaster mode, radius should already be maxed, but just in case:
                if (req.getCurrentRadiusKm() < maxRadiusKm) {
                    req.setCurrentRadiusKm(maxRadiusKm);
                    requestRepository.save(req);
                    logEventAndNotify(req, "RADIUS_EXPANDED", "Search radius expanded to " + req.getCurrentRadiusKm() + "km");
                }
                broadcastToDonors(req);
                continue;
            }

            // Normal mode: check if there are any eligible unnotified donors in CURRENT radius
            List<Donor> eligibleDonors = matchingEngine.findEligibleDonors(req, req.getCurrentRadiusKm());
            long unnotifiedCount = eligibleDonors.stream()
                .filter(donor -> requestResponseRepository.findByRequestIdAndDonorId(req.getId(), donor.getId()).isEmpty())
                .count();

            if (unnotifiedCount > 0) {
                // We still have donors in current radius. Just broadcast to the next batch.
                broadcastToDonors(req);
            } else if (req.getCurrentRadiusKm() < maxRadiusKm) {
                // No more donors in current radius, expand radius
                if (req.getCurrentRadiusKm() < 15) {
                    req.setCurrentRadiusKm(15);
                } else if (req.getCurrentRadiusKm() < 30) {
                    req.setCurrentRadiusKm(30);
                } else if (req.getCurrentRadiusKm() < maxRadiusKm) {
                    req.setCurrentRadiusKm(maxRadiusKm);
                }
                
                requestRepository.save(req);
                logEventAndNotify(req, "RADIUS_EXPANDED", "Search radius expanded to " + req.getCurrentRadiusKm() + "km");
                broadcastToDonors(req);
            } else {
                // At max radius and no more donors. Broadcast will trigger SOS fallback.
                broadcastToDonors(req);
            }
        }
    }

    // Expire old requests
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void expireRequests() {
        List<EmergencyRequest> activeRequests = requestRepository.findByStatus(RequestStatus.PENDING);
        activeRequests.addAll(requestRepository.findByStatus(RequestStatus.IN_PROGRESS));
        
        LocalDateTime now = LocalDateTime.now();
        for (EmergencyRequest req : activeRequests) {
            if (req.getExpiresAt() != null && req.getExpiresAt().isBefore(now)) {
                req.setStatus(RequestStatus.EXPIRED);
                requestRepository.save(req);
                logEventAndNotify(req, "STATUS_CHANGED", "Emergency request status changed to EXPIRED");
            }
        }
    }

    @Transactional
    public void updateResponseStatus(UUID requestId, String donorPhone, RequestResponseStatus status) {
        if (status != RequestResponseStatus.EN_ROUTE && status != RequestResponseStatus.DONATED && status != RequestResponseStatus.NO_SHOW) {
            throw new IllegalArgumentException("Invalid status transition for donor");
        }

        EmergencyRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + requestId));

        Donor donor = donorRepository.findByUserId(
                        userRepository.findByPhone(donorPhone)
                                .orElseThrow(() -> new IllegalArgumentException("User not found")).getId())
                .orElseThrow(() -> new IllegalArgumentException("Donor profile not found"));

        RequestResponse response = requestResponseRepository
                .findByRequestIdAndDonorId(requestId, donor.getId())
                .orElseThrow(() -> new IllegalStateException("Response not found for donor"));

        if (response.getStatus() != RequestResponseStatus.ACCEPTED && response.getStatus() != RequestResponseStatus.EN_ROUTE) {
            throw new IllegalStateException("Cannot transition response from current state: " + response.getStatus());
        }

        response.setStatus(status);
        response.setRespondedAt(LocalDateTime.now());
        requestResponseRepository.save(response);

        logResponseEventAndNotify(request, donor, status, "Donor status updated to: " + status);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<EmergencyRequest> getRequestsByRequester(String phone, org.springframework.data.domain.Pageable pageable) {
        com.lifelink.user.User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return requestRepository.findByRequesterId(user.getId(), pageable);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<RequestResponse> getRequestResponses(UUID requestId, String requesterPhone, org.springframework.data.domain.Pageable pageable) {
        EmergencyRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + requestId));
        
        com.lifelink.user.User user = userRepository.findByPhone(requesterPhone)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (!request.getRequester().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Not authorized to view responses for this request");
        }

        return requestResponseRepository.findByRequestId(requestId, pageable);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<RequestEvent> getRequestEvents(UUID requestId, String requesterPhone, org.springframework.data.domain.Pageable pageable) {
        EmergencyRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + requestId));

        com.lifelink.user.User user = userRepository.findByPhone(requesterPhone)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!request.getRequester().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Not authorized to view events for this request");
        }

        return requestEventRepository.findByRequestIdOrderByCreatedAtAsc(requestId, pageable);
    }

    private void logEventAndNotify(EmergencyRequest request, String eventType, String message) {
        RequestEvent event = new RequestEvent();
        event.setRequest(request);
        event.setEventType(eventType);
        event.setMessage(message);
        requestEventRepository.save(event);

        if (request.getRequester() != null) {
            webhookService.dispatchEvent(
                request.getRequester().getId(),
                com.lifelink.webhook.dto.WebhookEventPayload.builder()
                    .eventId(UUID.randomUUID())
                    .eventType(eventType)
                    .timestamp(LocalDateTime.now())
                    .requestId(request.getId())
                    .requestStatus(request.getStatus().name())
                    .bloodType(request.getBloodType())
                    .componentType(request.getComponentType().name())
                    .urgency(request.getUrgency().name())
                    .latitude(request.getLocation().getY())
                    .longitude(request.getLocation().getX())
                    .build()
            );
        }
    }

    private void logResponseEventAndNotify(EmergencyRequest request, Donor donor, RequestResponseStatus status, String message) {
        RequestEvent event = new RequestEvent();
        event.setRequest(request);
        event.setEventType("DONOR_RESPONDED");
        event.setMessage(message);
        requestEventRepository.save(event);

        if (request.getRequester() != null) {
            webhookService.dispatchEvent(
                request.getRequester().getId(),
                com.lifelink.webhook.dto.WebhookEventPayload.builder()
                    .eventId(UUID.randomUUID())
                    .eventType("DONOR_RESPONDED")
                    .timestamp(LocalDateTime.now())
                    .requestId(request.getId())
                    .requestStatus(request.getStatus().name())
                    .bloodType(request.getBloodType())
                    .componentType(request.getComponentType().name())
                    .urgency(request.getUrgency().name())
                    .latitude(request.getLocation().getY())
                    .longitude(request.getLocation().getX())
                    .detail(com.lifelink.webhook.dto.WebhookEventPayload.Detail.builder()
                        .donorId(donor.getId())
                        .responseStatus(status.name())
                        .build())
                    .build()
            );
        }
    }
}
