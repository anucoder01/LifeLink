package com.lifelink.notification;

import com.lifelink.donor.DonorRepository;
import com.lifelink.user.UserRepository;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/v1/donors/me/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DONOR')")
public class DonorNotificationPreferencesController {
    private final UserRepository userRepository;
    private final DonorRepository donorRepository;
    private final NotificationPreferenceRepository preferenceRepository;

    @Data public static class UpdatePreferencesRequest {
        private Boolean smsEnabled;
        private Boolean whatsappEnabled;
        private LocalTime silentStartTime;
        private LocalTime silentEndTime;
    }
    public record PreferencesDto(boolean smsEnabled, boolean whatsappEnabled, boolean smsOptedOut,
                                 LocalTime silentStartTime, LocalTime silentEndTime) {}

    @GetMapping
    public ResponseEntity<PreferencesDto> get(Authentication authentication) {
        return ResponseEntity.ok(map(getOrCreate(authentication.getName())));
    }

    @PutMapping
    public ResponseEntity<PreferencesDto> update(Authentication authentication, @Valid @RequestBody UpdatePreferencesRequest request) {
        NotificationPreference prefs = getOrCreate(authentication.getName());
        if (request.getSmsEnabled() != null) prefs.setSmsEnabled(request.getSmsEnabled());
        if (request.getWhatsappEnabled() != null) prefs.setWhatsappEnabled(request.getWhatsappEnabled());
        if (request.getSilentStartTime() != null || request.getSilentEndTime() != null) {
            if (request.getSilentStartTime() == null || request.getSilentEndTime() == null) return ResponseEntity.badRequest().build();
            prefs.setSilentStartTime(request.getSilentStartTime());
            prefs.setSilentEndTime(request.getSilentEndTime());
        }
        return ResponseEntity.ok(map(preferenceRepository.save(prefs)));
    }

    private NotificationPreference getOrCreate(String phone) {
        var user = userRepository.findByPhone(phone).orElseThrow(() -> new IllegalArgumentException("User not found"));
        var donor = donorRepository.findByUserId(user.getId()).orElseThrow(() -> new IllegalArgumentException("Donor profile not found"));
        return preferenceRepository.findByDonorId(donor.getId()).orElseGet(() -> {
            NotificationPreference prefs = new NotificationPreference();
            prefs.setDonor(donor);
            return preferenceRepository.save(prefs);
        });
    }
    private PreferencesDto map(NotificationPreference prefs) {
        return new PreferencesDto(Boolean.TRUE.equals(prefs.getSmsEnabled()), Boolean.TRUE.equals(prefs.getWhatsappEnabled()),
                Boolean.TRUE.equals(prefs.getSmsOptedOut()), prefs.getSilentStartTime(), prefs.getSilentEndTime());
    }
}
