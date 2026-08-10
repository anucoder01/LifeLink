package com.lifelink.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AppNotificationRepository extends JpaRepository<AppNotification, UUID> {
    Page<AppNotification> findByDonorUserId(UUID userId, Pageable pageable);
    boolean existsByDonorUserIdAndRelatedEntityId(UUID userId, String relatedEntityId);
}
