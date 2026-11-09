package com.lifelink.donor;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import org.springframework.security.core.Authentication;
import com.lifelink.user.UserRepository;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/donors")
@RequiredArgsConstructor
public class DonorRecoveryController {

    private final DonorRecoveryService recoveryService;
    private final UserRepository userRepository;
    private final DonorRepository donorRepository;

    @GetMapping("/me/recovery")
    public ResponseEntity<DonorRecoveryService.DonorRecoveryStatusDto> getRecoveryStatus(Authentication authentication) {
        UUID donorId = currentDonorId(authentication);
        return ResponseEntity.ok(recoveryService.getRecoveryStatus(donorId));
    }

    @PostMapping("/me/recovery/log")
    public ResponseEntity<DonorRecoveryService.DonorRecoveryStatusDto> logProgress(
            Authentication authentication,
            @Valid @RequestBody DonorRecoveryService.LogRecoveryDto dto) {
        UUID donorId = currentDonorId(authentication);
        dto.setDonorId(donorId);
        return ResponseEntity.ok(recoveryService.logProgress(dto));
    }

    private UUID currentDonorId(Authentication authentication) {
        var user = userRepository.findByPhone(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return donorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Donor profile not found")).getId();
    }
}
