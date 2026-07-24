package com.lifelink.bloodchain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DonorVouchRepository extends JpaRepository<DonorVouch, UUID> {

    List<DonorVouch> findAllByDonorId(UUID donorId);

    long countByDonorId(UUID donorId);

    Optional<DonorVouch> findByDonorIdAndContactPhone(UUID donorId, String contactPhone);

    boolean existsByDonorIdAndContactPhone(UUID donorId, String contactPhone);
}
