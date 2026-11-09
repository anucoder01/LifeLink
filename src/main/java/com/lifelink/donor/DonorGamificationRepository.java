package com.lifelink.donor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DonorGamificationRepository extends JpaRepository<DonorGamification, UUID> {
    Optional<DonorGamification> findByDonorId(UUID donorId);

    @Query("SELECT g FROM DonorGamification g JOIN FETCH g.donor d JOIN FETCH d.user u ORDER BY g.xp DESC")
    List<DonorGamification> findTopDonorsByXp();
}
