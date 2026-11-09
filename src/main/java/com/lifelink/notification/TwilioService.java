package com.lifelink.notification;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwilioService {

    private final SmsService smsService;

    public void sendSms(String to, String body) {
        try { smsService.send(to, body); }
        catch (RuntimeException e) { log.warn("SMS fallback failed for recipient ending {}: {}", to == null ? "" : to.replaceAll("\\D", "").replaceAll(".*(\\d{4})$", "$1"), e.getMessage()); }
    }

    public void sendWhatsApp(String to, String body) {
        try { smsService.sendWhatsApp(to, body); }
        catch (RuntimeException e) { log.warn("WhatsApp fallback delivery failed: {}", e.getMessage()); }
    }
}
