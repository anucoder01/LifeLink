package com.lifelink.webhook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateWebhookSubscriptionDto {

    @NotBlank(message = "Webhook URL is required")
    private String url;

    @NotBlank(message = "Secret is required")
    @Size(min = 8, max = 100, message = "Secret must be between 8 and 100 characters")
    private String secret;
}
