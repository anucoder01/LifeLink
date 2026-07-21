package com.lifelink.request;

import com.lifelink.donor.Donor;
import com.lifelink.matching.MatchingEngine;
import com.lifelink.notification.FcmService;
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
    private final MatchingEngine matchingEngine;
    private final FcmService fcmService;

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

    private void broadcastToDonors(EmergencyRequest request) {
        List<Donor> eligibleDonors = matchingEngine.findEligibleDonors(request, request.getCurrentRadiusKm());
        
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
