package com.lifelink.institution;

import com.lifelink.common.dto.PaginatedResponse;
import com.lifelink.common.util.PaginationUtil;
import com.lifelink.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Tag(name = "Hospital Forwarding", description = "Cross-institution emergency request forwarding")
@RestController
@RequestMapping("/api/v1/blood-banks/forwards")
@RequiredArgsConstructor
public class HospitalForwardController {

    private final HospitalForwardRepository hospitalForwardRepository;
    private final UserRepository userRepository;
    private final com.lifelink.bloodbank.BloodBankRepository bloodBankRepository;

    @Operation(summary = "Get all requests forwarded to the authenticated Blood Bank")
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<PaginatedResponse<HospitalForward>> getForwards(
            Authentication authentication,
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {

        com.lifelink.user.User user = userRepository.findByPhone(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        com.lifelink.bloodbank.BloodBank bloodBank = bloodBankRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Blood Bank profile not found"));

        Page<HospitalForward> page = hospitalForwardRepository.findByBloodBankId(bloodBank.getId(), pageable);
        return ResponseEntity.ok(PaginationUtil.fromPage(page));
    }

    @Operation(summary = "Accept a forwarded emergency request")
    @PutMapping("/{id}/accept")
    @Transactional
    public ResponseEntity<HospitalForward> acceptForward(
            @PathVariable UUID id,
            Authentication authentication) {
        
        com.lifelink.user.User user = userRepository.findByPhone(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        com.lifelink.bloodbank.BloodBank bloodBank = bloodBankRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Blood Bank profile not found"));

        HospitalForward forward = hospitalForwardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Forward request not found"));

        if (!forward.getBloodBank().getId().equals(bloodBank.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Not authorized");
        }

        forward.setStatus("ACCEPTED");
        hospitalForwardRepository.save(forward);

        return ResponseEntity.ok(forward);
    }

    @Operation(summary = "Decline a forwarded emergency request")
    @PutMapping("/{id}/decline")
    @Transactional
    public ResponseEntity<HospitalForward> declineForward(
            @PathVariable UUID id,
            Authentication authentication) {
        
        com.lifelink.user.User user = userRepository.findByPhone(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        com.lifelink.bloodbank.BloodBank bloodBank = bloodBankRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Blood Bank profile not found"));

        HospitalForward forward = hospitalForwardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Forward request not found"));

        if (!forward.getBloodBank().getId().equals(bloodBank.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Not authorized");
        }

        forward.setStatus("REJECTED");
        hospitalForwardRepository.save(forward);

        return ResponseEntity.ok(forward);
    }
}
