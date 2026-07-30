package com.lifelink.camp;

import com.lifelink.donor.Donor;
import com.lifelink.donor.DonorRepository;
import com.lifelink.notification.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampGeoService {

    private final CampRepository campRepository;
    private final DonorRepository donorRepository;
    private final FcmService fcmService;

    // Run every 1 hour to notify donors near active camps
    @Scheduled(fixedDelay = 3600000)
    @Transactional
    public void notifyDonorsNearCamps() {
        LocalDateTime now = LocalDateTime.now();
        List<Camp> activeCamps = campRepository.findActiveCamps(now);

        for (Camp camp : activeCamps) {
            // Find donors within camp radius
            // We reuse the basic native query from DonorRepository for distance check
            // For simplicity, we just fetch eligible active donors near this point
            // Ideally we should have a specific repository method that doesn't check blood type compatibility
            // but we can just use the existing one and pass a dummy list of all types, or just write a specific query.
            
            // Let's write a simple loop over all active donors for now, or use a custom query.
            // Using a new query `findActiveDonorsWithinRadius` would be better. Let's assume we add it to DonorRepository.
            
            List<Donor> nearbyDonors = donorRepository.findActiveDonorsWithinRadius(camp.getLocation(), camp.getRadiusMeters());
            
            // Filter donors who have consented to camp notifications
            List<Donor> notifiedDonors = nearbyDonors.stream()
                    .filter(d -> d.getConsent() == null || Boolean.TRUE.equals(d.getConsent().getAllowCampNotifications()))
                    .collect(Collectors.toList());

            for (Donor donor : notifiedDonors) {
                if (donor.getFcmToken() != null && !donor.getFcmToken().isEmpty()) {
                    fcmService.sendNotification(
                            donor.getFcmToken(), 
                            "Blood Camp Nearby!", 
                            "A blood donation camp is happening near you at " + camp.getName() + ".", 
                            false
                    );
                }
            }
            log.info("Notified {} donors near camp: {}", notifiedDonors.size(), camp.getName());
        }
    }
}
