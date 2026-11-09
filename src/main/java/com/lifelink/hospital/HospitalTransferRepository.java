package com.lifelink.hospital;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HospitalTransferRepository extends JpaRepository<HospitalTransfer, UUID> {
    List<HospitalTransfer> findByRequestingHospitalIdOrProvidingHospitalIdOrderByCreatedAtDesc(UUID reqId, UUID provId);
    List<HospitalTransfer> findByStatusOrderByCreatedAtDesc(String status);
}
