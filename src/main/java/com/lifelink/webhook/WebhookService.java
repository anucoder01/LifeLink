package com.lifelink.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelink.webhook.dto.WebhookEventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final WebhookSubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    /**
     * Finds active webhook subscriptions for a user and dispatches the event payload.
     *
     * @param userId  the user ID of the requester
     * @param payload the webhook payload details
     */
    public void dispatchEvent(UUID userId, WebhookEventPayload payload) {
        List<WebhookSubscription> subscriptions = subscriptionRepository.findByUserIdAndIsActiveTrue(userId);
        if (subscriptions.isEmpty()) {
            return;
        }

        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            for (WebhookSubscription sub : subscriptions) {
                sendWebhookAsync(sub.getUrl(), sub.getSecret(), jsonPayload);
            }
        } catch (Exception e) {
            log.error("Failed to serialize webhook payload for user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Asynchronously sends the webhook payload to the target URL with HMAC-SHA256 signature.
     */
    @Async
    public void sendWebhookAsync(String url, String secret, String jsonPayload) {
        try {
            String signature = calculateHmacSha256(jsonPayload, secret);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("X-LifeLink-Signature", signature)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            log.info("Sending webhook to {} with signature {}", url, signature);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Webhook successfully delivered to {}. Status code: {}", url, response.statusCode());
            } else {
                log.warn("Webhook delivery to {} returned status code: {}", url, response.statusCode());
            }
        } catch (Exception e) {
            log.error("Failed to deliver webhook to {}: {}", url, e.getMessage());
        }
    }

    private String calculateHmacSha256(String data, String key) throws Exception {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKey);
        byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
