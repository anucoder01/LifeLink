package com.lifelink.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    public void subscribeToTopic(String fcmToken, String topic) {
        if (fcmToken == null || topic == null) return;
        try {
            FirebaseMessaging.getInstance().subscribeToTopic(List.of(fcmToken), topic);
            log.debug("Subscribed token {} to topic {}", fcmToken, topic);
        } catch (Exception e) {
            log.error("Failed to subscribe token to topic", e);
        }
    }

    public void unsubscribeFromTopic(String fcmToken, String topic) {
        if (fcmToken == null || topic == null) return;
        try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(List.of(fcmToken), topic);
        } catch (Exception e) {
            log.error("Failed to unsubscribe token from topic", e);
        }
    }

    public void sendNotification(String fcmToken, String title, String body, boolean isCritical) {
        if (fcmToken == null) return;
        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    // Priority is set via android config for Critical
                    .putData("urgency", isCritical ? "CRITICAL" : "NORMAL")
                    .build();

            FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {
            log.error("Failed to send notification to token", e);
        }
    }

    public void sendStandDownNotification(String fcmToken, String requestId) {
        sendNotification(fcmToken, "Request Fulfilled", "Thank you, but the request has been fulfilled by someone else.", false);
    }
}
