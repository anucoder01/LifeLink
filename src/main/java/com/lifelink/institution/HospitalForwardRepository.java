package com.lifelink.institution;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HospitalForwardRepository extends JpaRepository<HospitalForward, UUID> {
    org.springframework.data.domain.Page<HospitalForward> findByBloodBankId(UUID bloodBankId, org.springframework.data.domain.Pageable pageable);
}
