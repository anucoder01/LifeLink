package com.lifelink.admin;

import com.lifelink.bloodbank.BloodBankService;
import com.lifelink.bloodbank.dto.BloodBankDto;
import com.lifelink.hospital.HospitalService;
import com.lifelink.hospital.dto.HospitalDto;
import com.lifelink.ngo.NgoService;
import com.lifelink.ngo.dto.NgoDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import org.springframework.security.core.Authentication;
import com.lifelink.user.UserRepository;
import com.lifelink.request.RequestService;
import com.lifelink.request.EmergencyRequest;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Tag(name = "Admin Portal", description = "Endpoints for SUPER_ADMIN to verify organizations")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminController {

    private final HospitalService hospitalService;
    private final BloodBankService bloodBankService;
    private final NgoService ngoService;
    private final RequestService requestService;
    private final UserRepository userRepository;
    private final com.lifelink.hospital.HospitalRepository hospitalRepository;
    private final PasswordEncoder passwordEncoder;

    public record HospitalAdminCreateRequest(@NotBlank @Size(max = 100) String name, @NotBlank @Size(max = 15) String phone,
            @NotBlank @Size(min = 12, max = 100) String password, UUID hospitalId) {}

    @Operation(summary = "Create a hospital administrator account")
    @PostMapping("/hospital-admins")
    public ResponseEntity<Void> createHospitalAdmin(@Valid @RequestBody HospitalAdminCreateRequest request) {
        if (userRepository.findByPhone(request.phone()).isPresent()) return ResponseEntity.badRequest().build();
        var hospital = hospitalRepository.findById(request.hospitalId()).orElseThrow(() -> new IllegalArgumentException("Hospital not found"));
        if (!Boolean.TRUE.equals(hospital.getVerified())) return ResponseEntity.badRequest().build();
        var user = new com.lifelink.user.User();
        user.setName(request.name());
        user.setPhone(request.phone());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(com.lifelink.user.Role.HOSPITAL_ADMIN);
        user.setHospital(hospital);
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List emergency requests awaiting verification")
    @GetMapping("/requests/pending-verification")
    public ResponseEntity<List<EmergencyRequest>> getPendingRequests() {
        return ResponseEntity.ok(requestService.getRequestsAwaitingVerification());
    }

    @Operation(summary = "Assign a hospital administrator account to a hospital")
    @PutMapping("/users/{userId}/hospital/{hospitalId}")
    public ResponseEntity<Void> assignHospitalAccount(@PathVariable UUID userId, @PathVariable UUID hospitalId) {
        var user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getRole() != com.lifelink.user.Role.HOSPITAL_ADMIN) {
            return ResponseEntity.badRequest().build();
        }
        var hospital = hospitalRepository.findById(hospitalId).orElseThrow(() -> new IllegalArgumentException("Hospital not found"));
        if (!Boolean.TRUE.equals(hospital.getVerified())) return ResponseEntity.badRequest().build();
        user.setHospital(hospital);
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Verify an emergency request and start donor matching")
    @PutMapping("/requests/{id}/verify")
    public ResponseEntity<EmergencyRequest> verifyRequest(Authentication authentication, @PathVariable UUID id) {
        UUID verifierId = userRepository.findByPhone(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found")).getId();
        return ResponseEntity.ok(requestService.verifyRequest(id, verifierId));
    }

    @Operation(summary = "Set hospital verification status")
    @PutMapping("/hospitals/{id}/verify")
    public ResponseEntity<HospitalDto> verifyHospital(
            @PathVariable UUID id,
            @RequestParam boolean verified) {
        return ResponseEntity.ok(hospitalService.setVerified(id, verified));
    }

    @Operation(summary = "Set blood bank verification status")
    @PutMapping("/blood-banks/{id}/verify")
    public ResponseEntity<BloodBankDto> verifyBloodBank(
            @PathVariable UUID id,
            @RequestParam boolean verified) {
        return ResponseEntity.ok(bloodBankService.setVerified(id, verified));
    }

    @Operation(summary = "Set NGO verification status")
    @PutMapping("/ngos/{id}/verify")
    public ResponseEntity<NgoDto> verifyNgo(
            @PathVariable UUID id,
            @RequestParam boolean verified) {
        return ResponseEntity.ok(ngoService.setVerified(id, verified));
    }
}
