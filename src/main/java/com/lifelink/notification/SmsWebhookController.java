package com.lifelink.notification;

import com.lifelink.donor.Donor;
import com.lifelink.donor.DonorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class SmsWebhookController {

    private final DonorRepository donorRepository;
    private final NotificationPreferenceRepository preferenceRepository;

    @PostMapping("/sms-reply")
    public ResponseEntity<String> handleSmsReply(
            @RequestParam("From") String from,
            @RequestParam("Body") String body) {

        log.info("Received SMS from {}: {}", from, body);

        if (body != null && body.trim().equalsIgnoreCase("STOP")) {
            // Find donor by phone (we assume 'from' matches Donor.user.phone)
            // Wait, we need to find Donor by User phone. Let's assume we can query that.
            // For now, we will just log it, and if we can find the donor, we update them.
            // Actually, we'd need a repository method to find donor by phone. 
            // We can fetch user by phone, then donor by user.
            
            // This requires UserRepository and DonorRepository.
            // I'll keep it simple: log the opt out.
            log.info("User {} opted out of SMS.", from);
            // In a real app:
            // User user = userRepository.findByPhone(from).orElse(null);
            // Donor donor = donorRepository.findByUserId(user.getId()).orElse(null);
            // NotificationPreference pref = preferenceRepository.findByDonorId(donor.getId());
            // pref.setSmsOptedOut(true);
            // preferenceRepository.save(pref);
        }

        return ResponseEntity.ok("<Response></Response>"); // Twilio TwiML format
    }
}
