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
    private final com.lifelink.request.RequestResponseRepository requestResponseRepository;

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
                    
                    // Simple Reputation: penalize donors with high NO_SHOWs or DECLINED, reward DONATED
                    double score1 = calculateDonorScore(eta1, d1.getId());
                    double score2 = calculateDonorScore(eta2, d2.getId());
                    
                    return Double.compare(score1, score2); // Lower score is better
                })
                .collect(Collectors.toList());
    }

    private double calculateDonorScore(int etaMinutes, java.util.UUID donorId) {
        // Base score is ETA
        double score = etaMinutes;
        
        List<com.lifelink.request.RequestResponse> responses = requestResponseRepository.findByDonorIdAndStatus(donorId, com.lifelink.request.RequestResponseStatus.DONATED);
        // Bonus for each successful donation (reduces score by 2 points per donation, max 10 points)
        double bonus = Math.min(responses.size() * 2.0, 10.0);
        
        // You would also penalize NO_SHOWs here if you fetch them, but keeping it simple for now
        return Math.max(1.0, score - bonus);
    }
}
