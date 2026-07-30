package com.lifelink.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TwilioService {

    public void sendSms(String to, String body) {
        log.info("[MOCK TWILIO SMS] To: {}, Body: {}", to, body);
    }

    public void sendWhatsApp(String to, String body) {
        log.info("[MOCK TWILIO WHATSAPP] To: {}, Body: {}", to, body);
    }
}
