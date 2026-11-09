package com.lifelink.hospital;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface BloodUnitLotRepository extends JpaRepository<BloodUnitLot, UUID> {
    List<BloodUnitLot> findByHospitalIdOrderByExpiresOnAsc(UUID hospitalId);
    List<BloodUnitLot> findByStatusAndExpiresOnBefore(LotStatus status, java.time.LocalDate date);
    long countByHospitalIdAndBloodTypeAndComponentType(UUID hospitalId, String bloodType, com.lifelink.request.ComponentType componentType);
    @Query("select coalesce(sum(l.unitCount), 0) from BloodUnitLot l where l.hospital.id = :hospitalId and l.bloodType = :bloodType and l.componentType = :componentType and l.status = :status and l.expiresOn >= current_date")
    Long sumActiveUnits(@Param("hospitalId") UUID hospitalId, @Param("bloodType") String bloodType,
                        @Param("componentType") com.lifelink.request.ComponentType componentType,
                        @Param("status") LotStatus status);
}
