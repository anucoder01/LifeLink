package com.lifelink.notification;

import com.lifelink.donor.Donor;
import com.lifelink.donor.DonorRepository;
import com.lifelink.request.EmergencyRequest;
import com.lifelink.request.RequestRepository;
import com.lifelink.request.RequestService;
import com.lifelink.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;
import org.springframework.util.MultiValueMap;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks/twilio")
@RequiredArgsConstructor
public class TwilioInboundWebhookController {

    private final DonorRepository donorRepository;
    private final RequestRepository requestRepository;
    private final RequestService requestService;
    private final TwilioSignatureValidator signatureValidator;
    private final UserRepository userRepository;
    private final NotificationPreferenceRepository preferenceRepository;

    @PostMapping(value = "/inbound-sms", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> handleInboundSms(
            @RequestHeader(value = "X-Twilio-Signature", required = false) String signature,
            @RequestParam MultiValueMap<String, String> parameters,
            @RequestParam(name = "From", required = false) String rawFromPhone,
            @RequestParam(name = "Body", required = false) String rawBody) {

        if (!signatureValidator.isValid(signature, parameters)) {
            return ResponseEntity.status(403).body("<?xml version=\"1.0\"?><Response></Response>");
        }

        String senderDigits = rawFromPhone == null ? "" : rawFromPhone.replaceAll("\\D", "");
        log.info("Received signed inbound provider message from recipient ending {}", senderDigits.substring(Math.max(0, senderDigits.length() - 4)));

        String fromPhone = rawFromPhone != null ? rawFromPhone.replace("whatsapp:", "").trim() : "";
        String body = rawBody != null ? rawBody.trim().toUpperCase() : "";

        if (body.equals("STOP") || body.equals("START") || body.equals("UNSTOP")) {
            updateSmsPreference(fromPhone, !body.equals("STOP"));
            String reply = body.equals("STOP") ? "LifeLink SMS alerts are turned off. Reply START to turn them back on." : "LifeLink SMS alerts are turned on.";
            return ResponseEntity.ok("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response><Message>" + reply + "</Message></Response>");
        }

        String replyMessage;

        try {
            Optional<Donor> donorOpt = userRepository.findByPhone(fromPhone)
                    .flatMap(user -> donorRepository.findByUserId(user.getId()));

            if (body.startsWith("YES") || body.startsWith("ACCEPT")) {
                replyMessage = handleAcceptCommand(donorOpt, body);
            } else if (body.startsWith("NO") || body.startsWith("DECLINE")) {
                replyMessage = handleDeclineCommand(donorOpt, body);
            } else if (body.contains("STATUS")) {
                replyMessage = donorOpt.map(d -> "LifeLink: Your donor availability is " + (Boolean.TRUE.equals(d.getIsActive()) ? "ACTIVE" : "INACTIVE") + ". Reply YES <full-request-id> or NO <full-request-id> to respond.")
                        .orElse("LifeLink: This phone is not linked to a donor account.");
            } else if (body.contains("ELIGIBILITY") || body.contains("CHECK")) {
                replyMessage = donorOpt.map(d -> "LifeLink: Your blood type is " + d.getBloodType() + ". Availability: " + (Boolean.TRUE.equals(d.getIsActive()) ? "ACTIVE" : "INACTIVE") + ".")
                        .orElse("LifeLink: Phone number not linked to an active donor profile.");
            } else {
            replyMessage = "LifeLink: Commands: YES <full-request-id>, NO <full-request-id>, STATUS, ELIGIBILITY, STOP, START.";
            }
        } catch (Exception e) {
            log.error("Error processing inbound SMS/WhatsApp", e);
            replyMessage = "LifeLink Bot: Request processed. Thank you for supporting the emergency blood network!";
        }

        String twimlResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<Response>\n" +
                "    <Message>" + replyMessage + "</Message>\n" +
                "</Response>";

        return ResponseEntity.ok(twimlResponse);
    }

    private void updateSmsPreference(String phone, boolean enabled) {
        userRepository.findByPhone(phone).flatMap(user -> donorRepository.findByUserId(user.getId()))
                .ifPresent(donor -> preferenceRepository.findByDonorId(donor.getId()).ifPresentOrElse(pref -> {
                    pref.setSmsOptedOut(!enabled);
                    pref.setSmsEnabled(enabled);
                    if (!enabled) pref.setWhatsappEnabled(false);
                    preferenceRepository.save(pref);
                }, () -> {
                    NotificationPreference pref = new NotificationPreference();
                    pref.setDonor(donor);
                    pref.setSmsOptedOut(!enabled);
                    pref.setSmsEnabled(enabled);
                    preferenceRepository.save(pref);
                }));
    }

    private String handleAcceptCommand(Optional<Donor> donorOpt, String body) {
        if (donorOpt.isEmpty()) {
            return "LifeLink: Your phone number is not recognized as a registered donor. Register at our web portal.";
        }
        Donor donor = donorOpt.get();
        String[] parts = body.split("\\s+");
        if (parts.length < 2) return "LifeLink: Please include the full request ID.";
        Optional<EmergencyRequest> reqOpt = findRequest(parts[1]);

        if (reqOpt.isEmpty()) {
            return "LifeLink: Emergency request not found or expired.";
        }

        try {
            String phone = donor.getUser() != null ? donor.getUser().getPhone() : "";
            requestService.respondToRequest(reqOpt.get().getId(), phone, com.lifelink.request.RequestResponseStatus.ACCEPTED);
            return "LifeLink: Your response was recorded. Thank you for helping.";
        } catch (Exception e) {
            return "LifeLink: Could not accept this request. It may be closed or not assigned to you.";
        }
    }

    private String handleDeclineCommand(Optional<Donor> donorOpt, String body) {
        if (donorOpt.isEmpty()) {
            return "LifeLink: Unregistered number.";
        }
        Donor donor = donorOpt.get();
        String[] parts = body.split("\\s+");
        if (parts.length < 2) return "LifeLink: Please include the full request ID.";
        Optional<EmergencyRequest> reqOpt = findRequest(parts[1]);

        if (reqOpt.isPresent()) {
            try {
                String phone = donor.getUser() != null ? donor.getUser().getPhone() : "";
                requestService.respondToRequest(reqOpt.get().getId(), phone, com.lifelink.request.RequestResponseStatus.DECLINED);
            } catch (Exception e) {
                return "LifeLink: Could not record your response. The request may be closed or not assigned to you.";
            }
        }
        return reqOpt.isPresent() ? "LifeLink: Your response was recorded." : "LifeLink: Emergency request not found or expired.";
    }

    private Optional<EmergencyRequest> findRequest(String value) {
        try { return requestRepository.findById(UUID.fromString(value)); }
        catch (IllegalArgumentException e) { return Optional.empty(); }
    }
}
