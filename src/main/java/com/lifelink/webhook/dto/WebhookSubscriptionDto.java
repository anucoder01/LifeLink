package com.lifelink.webhook.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class WebhookSubscriptionDto {
    private UUID id;
    private String url;
    private boolean active;
    private LocalDateTime createdAt;
}
