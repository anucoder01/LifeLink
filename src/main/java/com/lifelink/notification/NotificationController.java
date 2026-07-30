package com.lifelink.notification;

import com.lifelink.common.dto.PaginatedResponse;
import com.lifelink.common.util.PaginationUtil;
import com.lifelink.user.User;
import com.lifelink.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Notifications", description = "In-app notification center")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final AppNotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Operation(summary = "Get paginated in-app notifications")
    @GetMapping
    public ResponseEntity<PaginatedResponse<AppNotification>> getNotifications(
            Authentication authentication,
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {

        User user = userRepository.findByPhone(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Page<AppNotification> page = notificationRepository.findByDonorUserId(user.getId(), pageable);
        return ResponseEntity.ok(PaginationUtil.fromPage(page));
    }

    @Operation(summary = "Mark a notification as read")
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
        AppNotification notif = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        notif.setIsRead(true);
        notificationRepository.save(notif);
        return ResponseEntity.noContent().build();
    }
}
