package com.lifelink.hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface BloodUnitHandoffRepository extends JpaRepository<BloodUnitHandoff, UUID> {
    List<BloodUnitHandoff> findByToHospitalIdAndStatusOrderByRecordedAtDesc(UUID hospitalId, String status);
}
