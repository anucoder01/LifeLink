package com.lifelink.hospital;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Service
@RequiredArgsConstructor
public class HospitalTransferService {

    private final HospitalTransferRepository transferRepository;
    private final HospitalRepository hospitalRepository;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateTransferDto {
        @NotNull
        private UUID requestingHospitalId;
        private UUID providingHospitalId; // Optional specific target hospital
        @Size(max = 5)
        private String bloodType;
        @NotBlank
        private String componentType;
        @Min(1)
        private int unitsRequested;
        private String urgency;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HospitalTransferDto {
        private UUID id;
        private UUID requestingHospitalId;
        private String requestingHospitalName;
        private UUID providingHospitalId;
        private String providingHospitalName;
        private String bloodType;
        private String componentType;
        private int unitsRequested;
        private String urgency;
        private String status;
        private String notes;
        private LocalDateTime createdAt;
    }

    @Transactional
    public HospitalTransferDto createTransferRequest(CreateTransferDto dto) {
        Hospital requesting = hospitalRepository.findById(dto.getRequestingHospitalId())
                .orElseThrow(() -> new IllegalArgumentException("Requesting hospital not found"));
        if (!Boolean.TRUE.equals(requesting.getVerified())) throw new IllegalStateException("Requesting hospital is not verified");

        Hospital providing = null;
        if (dto.getProvidingHospitalId() != null) {
            providing = hospitalRepository.findById(dto.getProvidingHospitalId())
                    .orElseThrow(() -> new IllegalArgumentException("Providing hospital not found"));
            if (!Boolean.TRUE.equals(providing.getVerified())) throw new IllegalStateException("Providing hospital is not verified");
        }

        HospitalTransfer transfer = HospitalTransfer.builder()
                .requestingHospital(requesting)
                .providingHospital(providing)
                .bloodType(dto.getBloodType())
                .componentType(dto.getComponentType())
                .unitsRequested(dto.getUnitsRequested())
                .urgency(dto.getUrgency() != null ? dto.getUrgency() : "NORMAL")
                .status("REQUESTED")
                .notes(dto.getNotes())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        HospitalTransfer saved = transferRepository.save(transfer);
        return mapToDto(saved);
    }

    @Transactional
    public HospitalTransferDto updateTransferStatus(UUID transferId, UUID providingHospitalId, String status) {
        HospitalTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Transfer request not found"));

        if (providingHospitalId != null) {
            Hospital provider = hospitalRepository.findById(providingHospitalId).orElse(null);
            if (provider != null) {
                transfer.setProvidingHospital(provider);
            }
        }

        boolean allowed = switch (transfer.getStatus()) {
            case "REQUESTED" -> "ACCEPTED".equals(status) || "DECLINED".equals(status);
            case "ACCEPTED" -> "IN_TRANSIT".equals(status) || "DECLINED".equals(status);
            case "IN_TRANSIT" -> "COMPLETED".equals(status);
            default -> false;
        };
        if (!allowed) throw new IllegalStateException("Invalid transfer status transition");
        transfer.setStatus(status);
        transfer.setUpdatedAt(LocalDateTime.now());
        HospitalTransfer updated = transferRepository.save(transfer);
        return mapToDto(updated);
    }

    @Transactional(readOnly = true)
    public List<HospitalTransferDto> getHospitalTransfers(UUID hospitalId) {
        return transferRepository.findByRequestingHospitalIdOrProvidingHospitalIdOrderByCreatedAtDesc(hospitalId, hospitalId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HospitalTransferDto> getOpenBroadcastTransfers() {
        return transferRepository.findByStatusOrderByCreatedAtDesc("REQUESTED")
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private HospitalTransferDto mapToDto(HospitalTransfer entity) {
        return HospitalTransferDto.builder()
                .id(entity.getId())
                .requestingHospitalId(entity.getRequestingHospital().getId())
                .requestingHospitalName(entity.getRequestingHospital().getName())
                .providingHospitalId(entity.getProvidingHospital() != null ? entity.getProvidingHospital().getId() : null)
                .providingHospitalName(entity.getProvidingHospital() != null ? entity.getProvidingHospital().getName() : "Open Broadcast")
                .bloodType(entity.getBloodType())
                .componentType(entity.getComponentType())
                .unitsRequested(entity.getUnitsRequested())
                .urgency(entity.getUrgency())
                .status(entity.getStatus())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
