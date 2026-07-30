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

    private final TwilioService twilioService;
    private final NotificationPreferenceRepository preferenceRepository;
    private final AppNotificationRepository appNotificationRepository;

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
        // Backwards compatibility for callers without Donor context
        sendPush(fcmToken, title, body, isCritical);
    }

    public void sendNotificationToDonor(com.lifelink.donor.Donor donor, String title, String body, boolean isCritical, String entityId) {
        // 1. Check Preferences and Silent Hours
        NotificationPreference prefs = preferenceRepository.findByDonorId(donor.getId()).orElse(null);
        if (!isCritical && isSilentHour(prefs)) {
            log.info("Notification silenced for donor {} due to silent hours.", donor.getId());
            return;
        }

        // 2. Save In-App Notification
        AppNotification appNotif = new AppNotification();
        appNotif.setDonor(donor);
        appNotif.setTitle(title);
        appNotif.setBody(body);
        appNotif.setRelatedEntityId(entityId);
        appNotificationRepository.save(appNotif);

        // 3. Try FCM Push First
        boolean fcmSuccess = false;
        if (donor.getFcmToken() != null && !donor.getFcmToken().isEmpty()) {
            fcmSuccess = sendPush(donor.getFcmToken(), title, body, isCritical);
        }

        // 4. Fallback if FCM fails or no token
        if (!fcmSuccess && prefs != null) {
            String phone = donor.getUser().getPhone();
            if (phone != null) {
                if (Boolean.TRUE.equals(prefs.getSmsEnabled()) && !Boolean.TRUE.equals(prefs.getSmsOptedOut())) {
                    twilioService.sendSms(phone, title + ": " + body);
                }
                if (Boolean.TRUE.equals(prefs.getWhatsappEnabled())) {
                    twilioService.sendWhatsApp(phone, title + ": " + body);
                }
            }
        }
    }

    private boolean sendPush(String fcmToken, String title, String body, boolean isCritical) {
        if (fcmToken == null || fcmToken.isEmpty()) return false;
        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putData("urgency", isCritical ? "CRITICAL" : "NORMAL")
                    .build();

            FirebaseMessaging.getInstance().send(message);
            return true;
        } catch (Exception e) {
            log.error("Failed to send notification to token", e);
            return false;
        }
    }

    public void sendStandDownNotification(String fcmToken, String requestId) {
        sendNotification(fcmToken, "Request Fulfilled", "Thank you, but the request has been fulfilled by someone else.", false);
    }

    private boolean isSilentHour(NotificationPreference prefs) {
        if (prefs == null || prefs.getSilentStartTime() == null || prefs.getSilentEndTime() == null) {
            return false;
        }
        java.time.LocalTime now = java.time.LocalTime.now();
        java.time.LocalTime start = prefs.getSilentStartTime();
        java.time.LocalTime end = prefs.getSilentEndTime();

        if (start.isBefore(end)) {
            return now.isAfter(start) && now.isBefore(end);
        } else {
            // crosses midnight
            return now.isAfter(start) || now.isBefore(end);
        }
    }
}
