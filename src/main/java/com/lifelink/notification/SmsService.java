package com.lifelink.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    /**
     * Sends an SMS to the given phone number with the given message body.
     * If Twilio is not enabled, logs the message and returns without error.
     *
     * @param toPhone     recipient phone number in E.164 format (e.g. +919876543210)
     * @param messageBody the SMS text content
     */
    public void send(String toPhone, String messageBody) {
        if (!twilioEnabled) {
            log.info("[SMS STUB] To: {} | Message: {}", toPhone, messageBody);
            return;
        }

        // --- Real Twilio implementation (uncomment when Twilio SDK is added) ---
        // String accountSid = System.getenv("TWILIO_SID");
        // String authToken  = System.getenv("TWILIO_AUTH_TOKEN");
        // Twilio.init(accountSid, authToken);
        // Message.creator(
        //     new com.twilio.type.PhoneNumber(toPhone),
        //     new com.twilio.type.PhoneNumber(fromNumber),
        //     messageBody
        // ).create();
        // log.info("SMS sent to {}", toPhone);
        // -----------------------------------------------------------------------

        log.warn("[SMS] Twilio is enabled but SDK is not yet on classpath. " +
                 "Add com.twilio.sdk:twilio to pom.xml and uncomment the code in SmsService.");
    }
}
