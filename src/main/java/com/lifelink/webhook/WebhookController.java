package com.lifelink.webhook;

import com.lifelink.common.dto.PaginatedResponse;
import com.lifelink.common.util.PaginationUtil;
import com.lifelink.user.User;
import com.lifelink.user.UserRepository;
import com.lifelink.webhook.dto.CreateWebhookSubscriptionDto;
import com.lifelink.webhook.dto.WebhookSubscriptionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Tag(name = "Webhook Subscriptions", description = "Manage real-time state change webhook notifications")
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookSubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Operation(summary = "Register a new webhook subscription")
    @PostMapping
    public ResponseEntity<WebhookSubscriptionDto> subscribe(
            Authentication authentication,
            @Valid @RequestBody CreateWebhookSubscriptionDto dto) {

        User user = userRepository.findByPhone(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        WebhookSubscription sub = new WebhookSubscription();
        sub.setUser(user);
        sub.setUrl(dto.getUrl());
        sub.setSecret(dto.getSecret());
        sub.setActive(true);

        WebhookSubscription saved = subscriptionRepository.save(sub);
        WebhookSubscriptionDto responseDto = convertToDto(saved);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        return ResponseEntity.created(location).body(responseDto);
    }

    @Operation(summary = "List webhook subscriptions (paginated)")
    @GetMapping
    public ResponseEntity<PaginatedResponse<WebhookSubscriptionDto>> listSubscriptions(
            Authentication authentication,
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {

        User user = userRepository.findByPhone(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Page<WebhookSubscriptionDto> page = subscriptionRepository.findByUserId(user.getId(), pageable)
                .map(this::convertToDto);

        return ResponseEntity.ok(PaginationUtil.fromPage(page));
    }

    @Operation(summary = "Delete webhook subscription")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> unsubscribe(
            Authentication authentication,
            @PathVariable UUID id) {

        User user = userRepository.findByPhone(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        WebhookSubscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Webhook subscription not found"));

        if (!sub.getUser().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Not authorized to delete this subscription");
        }

        subscriptionRepository.delete(sub);
        return ResponseEntity.noContent().build();
    }

    private WebhookSubscriptionDto convertToDto(WebhookSubscription entity) {
        WebhookSubscriptionDto dto = new WebhookSubscriptionDto();
        dto.setId(entity.getId());
        dto.setUrl(entity.getUrl());
        dto.setActive(entity.isActive());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
