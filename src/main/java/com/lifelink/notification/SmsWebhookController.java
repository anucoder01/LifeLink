package com.lifelink.notification;

import com.lifelink.donor.DonorRepository;
import com.lifelink.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class SmsWebhookController {
    private final DonorRepository donorRepository;
    private final UserRepository userRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final TwilioSignatureValidator signatureValidator;

    @PostMapping(value = "/sms-reply", produces = MediaType.APPLICATION_XML_VALUE)
    @Transactional
    public ResponseEntity<String> handleSmsReply(
            @RequestHeader(value = "X-Twilio-Signature", required = false) String signature,
            @RequestParam MultiValueMap<String, String> parameters,
            @RequestParam("From") String from,
            @RequestParam("Body") String body) {
        if (!signatureValidator.isValid(signature, parameters)) {
            return ResponseEntity.status(403).body("<Response></Response>");
        }

        String command = body == null ? "" : body.trim().toUpperCase();
        if (command.equals("STOP") || command.equals("START") || command.equals("UNSTOP")) {
            String normalizedPhone = from.replace("whatsapp:", "").trim();
            userRepository.findByPhone(normalizedPhone).flatMap(user -> donorRepository.findByUserId(user.getId()))
                    .ifPresent(donor -> preferenceRepository.findByDonorId(donor.getId()).ifPresentOrElse(pref -> {
                        pref.setSmsOptedOut(command.equals("STOP"));
                        pref.setSmsEnabled(!command.equals("STOP"));
                        if (command.equals("STOP")) pref.setWhatsappEnabled(false);
                        preferenceRepository.save(pref);
                    }, () -> {
                        NotificationPreference pref = new NotificationPreference();
                        pref.setDonor(donor);
                        pref.setSmsOptedOut(command.equals("STOP"));
                        pref.setSmsEnabled(!command.equals("STOP"));
                        preferenceRepository.save(pref);
                    }));
            String digits = from.replaceAll("\\D", "");
            log.info("SMS preference command processed for recipient ending {}", digits.substring(Math.max(0, digits.length() - 4)));
        }
        return ResponseEntity.ok("<Response></Response>");
    }
}
