package com.lifelink.hospital;

import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface HospitalRepository extends JpaRepository<Hospital, UUID> {

    /**
     * Returns all verified hospitals whose centroid is within {@code radiusMeters}
     * of the given point, ordered nearest-first.
     */
    @Query("""
            SELECT h FROM Hospital h
            WHERE ST_DWithin(h.location, :origin, :radiusMeters) = true
            ORDER BY ST_Distance(h.location, :origin) ASC
            """)
    List<Hospital> findWithinRadius(
            @Param("origin") Point origin,
            @Param("radiusMeters") double radiusMeters);

    /**
     * Returns all verified hospitals whose centroid is within {@code radiusMeters}
     * and that have at least 1 unit of the requested blood type + component.
     */
    @Query("""
            SELECT DISTINCT h FROM Hospital h
            JOIN BloodInventory bi ON bi.hospital = h
            WHERE ST_DWithin(h.location, :origin, :radiusMeters) = true
              AND h.verified = true
              AND bi.bloodType = :bloodType
              AND bi.componentType = :componentType
              AND bi.unitsAvailable > 0
            ORDER BY ST_Distance(h.location, :origin) ASC
            """)
    List<Hospital> findNearbyWithAvailableBlood(
            @Param("origin") Point origin,
            @Param("radiusMeters") double radiusMeters,
            @Param("bloodType") String bloodType,
            @Param("componentType") String componentType);
}
