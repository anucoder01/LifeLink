package com.lifelink.hospital;

import com.lifelink.request.ComponentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BloodInventoryRepository extends JpaRepository<BloodInventory, UUID> {

    List<BloodInventory> findByHospitalId(UUID hospitalId);

    Optional<BloodInventory> findByHospitalIdAndBloodTypeAndComponentType(
            UUID hospitalId, String bloodType, ComponentType componentType);

    /**
     * Aggregated availability across all verified hospitals for a given
     * blood type + component.
     */
    @Query("""
            SELECT SUM(bi.unitsAvailable) FROM BloodInventory bi
            JOIN bi.hospital h
            WHERE h.verified = true
              AND bi.bloodType = :bloodType
              AND bi.componentType = :componentType
            """)
    Optional<Long> sumAvailableUnits(
            @Param("bloodType") String bloodType,
            @Param("componentType") ComponentType componentType);
}
