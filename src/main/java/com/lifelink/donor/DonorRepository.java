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

    @Query("SELECT d FROM Donor d WHERE d.isActive = true AND d.bloodType IN :compatibleBloodTypes AND ST_DWithin(d.location, :location, :radiusMeters) = true")
    List<Donor> findEligibleDonorsWithinRadius(@Param("location") Point location, @Param("radiusMeters") double radiusMeters, @Param("compatibleBloodTypes") List<String> compatibleBloodTypes);
}
