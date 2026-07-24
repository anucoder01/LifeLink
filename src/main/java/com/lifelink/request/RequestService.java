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

    @Transactional
    public EmergencyRequest createRequest(EmergencyRequest request) {
        request.setStatus(RequestStatus.PENDING);
        request.setCurrentRadiusKm(5);
        if (request.getExpiresAt() == null) {
            request.setExpiresAt(LocalDateTime.now().plusHours(24));
        }
        EmergencyRequest saved = requestRepository.save(request);
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

        if (newStatus == RequestResponseStatus.ACCEPTED
                && request.getStatus() == RequestStatus.PENDING) {
            request.setStatus(RequestStatus.IN_PROGRESS);
            requestRepository.save(request);
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

        if (eligibleDonors.isEmpty() && request.getCurrentRadiusKm() >= 30) {
            // ----------------------------------------------------------------
            // BLOOD CHAIN ACTIVATION: No eligible donors found within 30km.
            // Trigger SMS invites to trusted contacts of all active donors.
            // ----------------------------------------------------------------
            log.warn("No donors found at 30km radius for request {}. Activating Blood Chain.",
                    request.getId());
            bloodChainService.activateBloodChain(request);
            return;
        }

        for (Donor donor : eligibleDonors) {
            // Check if already notified
            if (requestResponseRepository.findByRequestIdAndDonorId(request.getId(), donor.getId()).isEmpty()) {
                RequestResponse response = new RequestResponse();
                response.setRequest(request);
                response.setDonor(donor);
                response.setStatus(RequestResponseStatus.NOTIFIED);
                requestResponseRepository.save(response);

                fcmService.sendNotification(donor.getFcmToken(), "Emergency Blood Request",
                        "Blood type " + request.getBloodType() + " is needed urgently.",
                        request.getUrgency() == Urgency.CRITICAL);
            }
        }
    }

    // Auto-expand radius every 5 minutes if no response
    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void autoExpandRadius() {
        List<EmergencyRequest> activeRequests = requestRepository.findByStatus(RequestStatus.PENDING);
        for (EmergencyRequest req : activeRequests) {
            if (req.getCurrentRadiusKm() < 30) {
                if (req.getCurrentRadiusKm() == 5) req.setCurrentRadiusKm(15);
                else if (req.getCurrentRadiusKm() == 15) req.setCurrentRadiusKm(30);
                
                requestRepository.save(req);
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
            }
        }
    }
}
