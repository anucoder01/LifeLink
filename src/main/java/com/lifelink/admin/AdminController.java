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

@Tag(name = "Admin Portal", description = "Endpoints for SUPER_ADMIN to verify organizations")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
// Assuming method security is enabled, if not we check it in a filter, but this is standard Spring Security
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminController {

    private final HospitalService hospitalService;
    private final BloodBankService bloodBankService;
    private final NgoService ngoService;

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
