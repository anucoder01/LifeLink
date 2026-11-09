package com.lifelink.hospital;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface BloodUnitTemperatureLogRepository extends JpaRepository<BloodUnitTemperatureLog, UUID> {
    List<BloodUnitTemperatureLog> findByLotIdOrderByRecordedAtDesc(UUID lotId);
}
