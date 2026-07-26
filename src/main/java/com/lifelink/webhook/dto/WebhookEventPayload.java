package com.lifelink.webhook.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class WebhookEventPayload {
    private UUID eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private UUID requestId;
    private String requestStatus;
    private String bloodType;
    private String componentType;
    private String urgency;
    private Double latitude;
    private Double longitude;
    private Detail detail;

    @Data
    @Builder
    public static class Detail {
        private UUID donorId;
        private String responseStatus;
    }
}
