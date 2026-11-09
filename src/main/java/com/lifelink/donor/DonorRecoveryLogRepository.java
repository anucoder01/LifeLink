package com.lifelink.donor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DonorRecoveryLogRepository extends JpaRepository<DonorRecoveryLog, UUID> {
    List<DonorRecoveryLog> findByDonorIdOrderByDayNumberAsc(UUID donorId);
    Optional<DonorRecoveryLog> findByDonorIdAndDayNumber(UUID donorId, int dayNumber);
    List<DonorRecoveryLog> findByDonorIdAndDonationDateOrderByDayNumberAsc(UUID donorId, java.time.LocalDate donationDate);
    Optional<DonorRecoveryLog> findByDonorIdAndDonationDateAndDayNumber(UUID donorId, java.time.LocalDate donationDate, int dayNumber);
}
