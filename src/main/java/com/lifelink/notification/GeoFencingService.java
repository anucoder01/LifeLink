package com.lifelink.notification;

import com.lifelink.donor.Donor;
import com.lifelink.donor.DonorRepository;
import com.lifelink.request.EmergencyRequest;
import com.lifelink.request.RequestRepository;
import com.lifelink.request.RequestStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeoFencingService {

    private final RequestRepository requestRepository;
    private final DonorRepository donorRepository;
    private final FcmService fcmService;
    private final AppNotificationRepository appNotificationRepository;

    /**
     * Checks active disaster mode requests and notifies donors within the expanded radius.
     * Runs every 10 minutes.
     */
    @Scheduled(fixedDelay = 600000)
    @Transactional
    public void processGeoFencingAlerts() {
        List<EmergencyRequest> disasterRequests = requestRepository.findByStatus(RequestStatus.PENDING).stream()
                .filter(EmergencyRequest::isDisasterMode)
                .collect(Collectors.toList());

        for (EmergencyRequest request : disasterRequests) {
            double radiusMeters = request.getCurrentRadiusKm() * 1000.0;
            // Get all donors in the disaster radius, regardless of blood type,
            // as they might be able to help with transport, logistics, or general aid
            List<Donor> donorsInRange = donorRepository.findActiveDonorsWithinRadius(request.getLocation(), radiusMeters);

            for (Donor donor : donorsInRange) {
                // Check if we already notified them recently to avoid spamming
                if (!hasRecentGeoFenceAlert(donor.getUser().getId(), request.getId())) {
                    sendGeoFenceAlert(donor, request);
                }
            }
        }
    }

    private boolean hasRecentGeoFenceAlert(java.util.UUID userId, java.util.UUID requestId) {
        // Simplified check: if an AppNotification exists for this request
        return appNotificationRepository.existsByDonorUserIdAndRelatedEntityId(userId, requestId.toString());
    }

    private void sendGeoFenceAlert(Donor donor, EmergencyRequest request) {
        String title = "⚠️ Active Disaster Zone";
        String body = "You are in an area with a critical emergency/disaster. Blood and volunteers are urgently needed.";
        
        AppNotification notif = new AppNotification();
        notif.setDonor(donor);
        notif.setTitle(title);
        notif.setBody(body);
        notif.setRelatedEntityId(request.getId().toString());
        appNotificationRepository.save(notif);

        fcmService.sendNotificationToDonor(donor, title, body, true, request.getId().toString());
        log.info("Sent geo-fence alert to donor {} for disaster request {}", donor.getId(), request.getId());
    }
}
