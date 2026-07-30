package com.lifelink.donor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DonorConsentRepository extends JpaRepository<DonorConsent, UUID> {
}
