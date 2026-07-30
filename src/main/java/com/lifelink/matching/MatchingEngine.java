package com.lifelink.matching;

import com.lifelink.donor.Donor;
import com.lifelink.donor.DonorRepository;
import com.lifelink.request.EmergencyRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingEngine {

    private final DonorRepository donorRepository;
    private final CompatibilityMatrix compatibilityMatrix;
    private final DistanceMatrixMockService distanceMatrixMockService;

    public List<Donor> findEligibleDonors(EmergencyRequest request, int radiusKm) {
        // compatibilityMatrix is a Spring bean — call goes through the cache proxy
        List<String> compatibleTypes = compatibilityMatrix.getCompatibleDonors(request.getBloodType());
        if (compatibleTypes.isEmpty()) {
            return List.of();
        }

        // 1 km = 1000 meters
        double radiusMeters = radiusKm * 1000.0;

        List<Donor> donorsInRange = donorRepository.findEligibleDonorsWithinRadius(
                request.getLocation(), radiusMeters, compatibleTypes);

        return donorsInRange.stream()
                .filter(d -> EligibilityUtil.isEligible(d, request.getComponentType()))
                .filter(d -> d.getConsent() == null || Boolean.TRUE.equals(d.getConsent().getAllowEmergencyNotifications()))
                .sorted((d1, d2) -> {
                    int eta1 = distanceMatrixMockService.estimateTravelTimeMinutes(d1.getLocation(), request.getLocation());
                    int eta2 = distanceMatrixMockService.estimateTravelTimeMinutes(d2.getLocation(), request.getLocation());
                    return Integer.compare(eta1, eta2);
                })
                .collect(Collectors.toList());
    }
}
