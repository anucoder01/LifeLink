package com.lifelink.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Sends SMS messages to phone numbers.
 *
 * Currently a STUB implementation — logs the message instead of actually sending.
 * To activate real SMS: set twilio.enabled=true in application.yml and provide
 * TWILIO_SID, TWILIO_AUTH_TOKEN, TWILIO_FROM_NUMBER environment variables.
 *
 * Twilio SDK dependency to add when enabling (in pom.xml):
 * <dependency>
 *   <groupId>com.twilio.sdk</groupId>
 *   <artifactId>twilio</artifactId>
 *   <version>10.4.1</version>
 * </dependency>
 */
@Slf4j
@Service
public class SmsService {

    @Value("${twilio.enabled:false}")
    private boolean twilioEnabled;

    @Value("${twilio.from-number:+10000000000}")
    private String fromNumber;

    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    @Value("${twilio.whatsapp-from-number:}")
    private String whatsappFromNumber;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Sends an SMS to the given phone number with the given message body.
     * If Twilio is not enabled, logs the message and returns without error.
     *
     * @param toPhone     recipient phone number in E.164 format (e.g. +919876543210)
     * @param messageBody the SMS text content
     */
    public void send(String toPhone, String messageBody) {
        if (!twilioEnabled) {
            log.info("[SMS STUB] Message suppressed for recipient ending {} (provider disabled)", lastFour(toPhone));
            return;
        }

        if (accountSid.isBlank() || authToken.isBlank() || fromNumber.isBlank()) {
            throw new IllegalStateException("Twilio is enabled but credentials or sender number are missing");
        }
        submitMessage(toPhone, messageBody, fromNumber);
    }

    public void sendWhatsApp(String toPhone, String messageBody) {
        if (!twilioEnabled) {
            log.info("[WHATSAPP STUB] Message suppressed for recipient ending {} (provider disabled)", lastFour(toPhone));
            return;
        }
        if (whatsappFromNumber.isBlank()) throw new IllegalStateException("Twilio WhatsApp sender is not configured");
        String recipient = toPhone.startsWith("whatsapp:") ? toPhone : "whatsapp:" + toPhone;
        String sender = whatsappFromNumber.startsWith("whatsapp:") ? whatsappFromNumber : "whatsapp:" + whatsappFromNumber;
        submitMessage(recipient, messageBody, sender);
    }

    private void submitMessage(String toPhone, String messageBody, String sender) {
        if (accountSid.isBlank() || authToken.isBlank() || sender.isBlank()) {
            throw new IllegalStateException("Twilio is enabled but credentials or sender number are missing");
        }
        String form = "To=" + encode(toPhone) + "&From=" + encode(sender) + "&Body=" + encode(messageBody);
        String credentials = Base64.getEncoder().encodeToString((accountSid + ":" + authToken).getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json"))
                .header("Authorization", "Basic " + credentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
        try {
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Twilio rejected SMS with HTTP " + response.statusCode());
            }
            log.info("Message submitted to provider for recipient ending {}", lastFour(toPhone));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("SMS delivery was interrupted", e);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("SMS delivery failed", e);
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String lastFour(String phone) {
        String digits = phone == null ? "" : phone.replaceAll("\\D", "");
        return digits.substring(Math.max(0, digits.length() - 4));
    }
}
