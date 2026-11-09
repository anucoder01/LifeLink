package com.lifelink.donor;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import com.lifelink.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/donors")
@RequiredArgsConstructor
public class GamificationController {

    private final GamificationService gamificationService;
    private final UserRepository userRepository;
    private final DonorRepository donorRepository;

    @GetMapping("/me/gamification")
    public ResponseEntity<GamificationService.GamificationProfileDto> getGamification(Authentication authentication) {
        UUID donorId = currentDonorId(authentication);
        return ResponseEntity.ok(gamificationService.getGamificationProfile(donorId));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/{donorId}/gamification/record")
    public ResponseEntity<GamificationService.GamificationProfileDto> recordDonation(
            @PathVariable UUID donorId,
            @RequestParam(defaultValue = "false") boolean isCritical) {
        return ResponseEntity.ok(gamificationService.recordDonation(donorId, isCritical));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<GamificationService.LeaderboardEntryDto>> getLeaderboard() {
        return ResponseEntity.ok(gamificationService.getLeaderboard());
    }

    @GetMapping("/me/certificate")
    public ResponseEntity<GamificationService.CertificateDto> getCertificate(Authentication authentication) {
        UUID donorId = currentDonorId(authentication);
        return ResponseEntity.ok(gamificationService.generateCertificate(donorId));
    }

    private UUID currentDonorId(Authentication authentication) {
        var user = userRepository.findByPhone(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return donorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Donor profile not found")).getId();
    }
}
