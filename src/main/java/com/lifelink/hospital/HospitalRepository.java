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
    @Query(value = "SELECT h.* FROM hospitals h WHERE ST_DWithin(h.location, :origin, :radiusMeters) ORDER BY ST_Distance(h.location, :origin) ASC", nativeQuery = true)
    List<Hospital> findWithinRadius(
            @Param("origin") Point origin,
            @Param("radiusMeters") double radiusMeters);

    /**
     * Returns all verified hospitals whose centroid is within {@code radiusMeters}
     * and that have at least 1 unit of the requested blood type + component.
     */
    @Query(value = "SELECT DISTINCT h.* FROM hospitals h " +
            "JOIN blood_inventory bi ON bi.hospital_id = h.id " +
            "WHERE ST_DWithin(h.location, :origin, :radiusMeters) " +
            "  AND h.verified = true " +
            "  AND bi.blood_type = :bloodType " +
            "  AND bi.component_type = :componentType " +
            "  AND bi.units_available > 0 " +
            "ORDER BY ST_Distance(h.location, :origin) ASC", nativeQuery = true)
    List<Hospital> findNearbyWithAvailableBlood(
            @Param("origin") Point origin,
            @Param("radiusMeters") double radiusMeters,
            @Param("bloodType") String bloodType,
            @Param("componentType") String componentType);
}
