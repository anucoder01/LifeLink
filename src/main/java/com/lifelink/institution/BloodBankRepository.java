package com.lifelink.institution;

import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BloodBankRepository extends JpaRepository<BloodBank, UUID> {
    
    @Query(value = """
        SELECT b.* FROM blood_banks b
        WHERE b.is_active = true
        AND ST_DWithin(
            b.location,
            :center,
            :radiusMeters,
            false
        )
    """, nativeQuery = true)
    List<BloodBank> findActiveBloodBanksWithinRadius(
            @Param("center") Point center,
            @Param("radiusMeters") double radiusMeters
    );
}
