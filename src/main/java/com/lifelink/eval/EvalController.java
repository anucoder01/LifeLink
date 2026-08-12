package com.lifelink.eval;

import com.lifelink.matching.MatchingEngine;
import com.lifelink.request.ComponentType;
import com.lifelink.request.EmergencyRequest;
import com.lifelink.request.Urgency;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/v1/eval")
@RequiredArgsConstructor
public class EvalController {

    private final MatchingEngine matchingEngine;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    @GetMapping("/benchmark")
    public ResponseEntity<Map<String, Object>> benchmarkMatchingEngine() {
        log.info("Starting matching engine benchmark...");
        int iterations = 100;
        long totalTimeNs = 0;
        int totalDonorsFound = 0;

        for (int i = 0; i < iterations; i++) {
            EmergencyRequest request = new EmergencyRequest();
            request.setId(UUID.randomUUID());
            request.setBloodType(getRandomBloodType());
            request.setComponentType(ComponentType.WHOLE_BLOOD);
            request.setUrgency(Urgency.HIGH);
            
            // Random point around Bangalore (approx 12.9716, 77.5946)
            double lat = 12.9716 + (Math.random() - 0.5) * 0.1;
            double lng = 77.5946 + (Math.random() - 0.5) * 0.1;
            Point loc = geometryFactory.createPoint(new Coordinate(lng, lat));
            loc.setSRID(4326);
            request.setLocation(loc);

            long startTime = System.nanoTime();
            int donorsFound = matchingEngine.findEligibleDonors(request, 15).size();
            long endTime = System.nanoTime();

            totalTimeNs += (endTime - startTime);
            totalDonorsFound += donorsFound;
        }

        double avgTimeMs = (totalTimeNs / (double) iterations) / 1_000_000.0;
        
        Map<String, Object> results = new HashMap<>();
        results.put("iterations", iterations);
        results.put("averageTimeMs", avgTimeMs);
        results.put("totalDonorsFound", totalDonorsFound);
        results.put("avgDonorsPerQuery", totalDonorsFound / (double) iterations);
        results.put("timestamp", LocalDateTime.now());

        log.info("Benchmark complete. Avg time: {} ms", avgTimeMs);
        return ResponseEntity.ok(results);
    }

    private String getRandomBloodType() {
        String[] types = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        return types[(int) (Math.random() * types.length)];
    }
}
