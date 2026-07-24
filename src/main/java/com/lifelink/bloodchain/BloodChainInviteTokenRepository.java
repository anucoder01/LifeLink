package com.lifelink.bloodchain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface BloodChainInviteTokenRepository extends JpaRepository<BloodChainInviteToken, UUID> {

    Optional<BloodChainInviteToken> findByToken(String token);

    /** True if this exact phone was already invited for this request (prevents double-SMS). */
    boolean existsByRequestIdAndContactPhone(UUID requestId, String contactPhone);

    /** Cleanup job: delete expired tokens that were never used, older than 7 days. */
    @Modifying
    @Query("DELETE FROM BloodChainInviteToken t WHERE t.used = false AND t.expiresAt < :cutoff")
    void deleteExpiredUnusedTokens(@Param("cutoff") LocalDateTime cutoff);
}
