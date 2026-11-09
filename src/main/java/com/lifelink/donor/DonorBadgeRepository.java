package com.lifelink.donor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DonorBadgeRepository extends JpaRepository<DonorBadge, UUID> {
    List<DonorBadge> findByDonorId(UUID donorId);
    boolean existsByDonorIdAndBadgeKey(UUID donorId, String badgeKey);
}
