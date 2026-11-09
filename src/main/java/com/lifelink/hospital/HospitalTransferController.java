package com.lifelink.hospital;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/hospitals/transfers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('HOSPITAL_ADMIN') or hasRole('SUPER_ADMIN')")
public class HospitalTransferController {

    private final HospitalTransferService transferService;
    private final HospitalAccessService accessService;
    private final HospitalTransferRepository transferRepository;

    @PostMapping
    public ResponseEntity<HospitalTransferService.HospitalTransferDto> createTransfer(
            Authentication authentication, @Valid @RequestBody HospitalTransferService.CreateTransferDto dto) {
        accessService.assertCanAccess(authentication.getName(), dto.getRequestingHospitalId());
        return ResponseEntity.ok(transferService.createTransferRequest(dto));
    }

    @PutMapping("/{transferId}/status")
    public ResponseEntity<HospitalTransferService.HospitalTransferDto> updateStatus(
            Authentication authentication,
            @PathVariable UUID transferId,
            @RequestParam UUID providingHospitalId,
            @RequestParam String status) {
        accessService.assertCanAccess(authentication.getName(), providingHospitalId);
        var existing = transferRepository.findById(transferId).orElseThrow(() -> new IllegalArgumentException("Transfer request not found"));
        if (existing.getProvidingHospital() != null && !existing.getProvidingHospital().getId().equals(providingHospitalId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(transferService.updateTransferStatus(transferId, providingHospitalId, status));
    }

    @GetMapping("/hospital/{hospitalId}")
    public ResponseEntity<List<HospitalTransferService.HospitalTransferDto>> getHospitalTransfers(
            Authentication authentication,
            @PathVariable UUID hospitalId) {
        accessService.assertCanAccess(authentication.getName(), hospitalId);
        return ResponseEntity.ok(transferService.getHospitalTransfers(hospitalId));
    }

    @GetMapping("/open")
    public ResponseEntity<List<HospitalTransferService.HospitalTransferDto>> getOpenBroadcastTransfers() {
        return ResponseEntity.ok(transferService.getOpenBroadcastTransfers());
    }
}
