package com.lifelink.donor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.locationtech.jts.geom.Point;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DonorRepository extends JpaRepository<Donor, UUID> {
    Optional<Donor> findByUserId(UUID userId);

    @Query(value = "SELECT d.* FROM donors d WHERE d.is_active = true AND d.blood_type IN :compatibleBloodTypes AND ST_DWithin(d.location, :location, :radiusMeters)", nativeQuery = true)
    List<Donor> findEligibleDonorsWithinRadius(@Param("location") Point location, @Param("radiusMeters") double radiusMeters, @Param("compatibleBloodTypes") List<String> compatibleBloodTypes);
    @Query(value = """
        SELECT d.* FROM donors d
        WHERE d.is_active = true
        AND ST_DWithin(
            d.location,
            :center,
            :radiusMeters,
            false
        )
    """, nativeQuery = true)
    List<Donor> findActiveDonorsWithinRadius(
            @Param("center") Point center,
            @Param("radiusMeters") double radiusMeters
    );
}
