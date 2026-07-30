package com.lifelink.matching;

import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class DistanceMatrixMockService {

    private final Random random = new Random();
    
    // Average city speed in meters per minute (e.g. 40 km/h = ~667 meters/min)
    private static final double METERS_PER_MINUTE = 666.67;

    /**
     * Estimates travel time (in minutes) between two points.
     * Uses straight-line distance, dividing by average speed, and adding a random traffic penalty (0-20%).
     */
    public int estimateTravelTimeMinutes(Point source, Point destination) {
        if (source == null || destination == null) {
            return Integer.MAX_VALUE;
        }

        // Calculate straight line distance using Haversine or simple euclidean if SRID allows
        // Here we'll use a basic Haversine formula approximation for demo
        double distanceMeters = calculateHaversineDistance(
                source.getY(), source.getX(),
                destination.getY(), destination.getX()
        );

        double baseTimeMinutes = distanceMeters / METERS_PER_MINUTE;
        
        // Add random traffic delay up to 20%
        double trafficMultiplier = 1.0 + (random.nextDouble() * 0.2);
        
        return (int) Math.ceil(baseTimeMinutes * trafficMultiplier);
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // Distance in km
        return distance * 1000; // Return in meters
    }
}
