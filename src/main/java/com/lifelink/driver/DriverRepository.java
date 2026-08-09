package com.lifelink.driver;

import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID> {
    Optional<Driver> findByUserId(UUID userId);

    @Query(value = """
        SELECT d.* FROM drivers d
        WHERE d.is_available = true
        AND d.verified = true
        AND ST_DWithin(
            d.location,
            :center,
            :radiusMeters,
            false
        )
    """, nativeQuery = true)
    List<Driver> findAvailableDriversWithinRadius(
            @Param("center") Point center,
            @Param("radiusMeters") double radiusMeters
    );
}
