package com.lifelink.hospital;

import com.lifelink.request.ComponentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hospitals/{hospitalId}/lots")
@RequiredArgsConstructor
@PreAuthorize("hasRole('HOSPITAL_ADMIN') or hasRole('SUPER_ADMIN')")
@Transactional
public class BloodUnitLotController {
    private final BloodUnitLotRepository lotRepository;
    private final BloodUnitTemperatureLogRepository temperatureRepository;
    private final HospitalRepository hospitalRepository;
    private final BloodInventoryRepository inventoryRepository;
    private final BloodUnitHandoffRepository handoffRepository;
    private final HospitalAccessService accessService;

    @Data
    public static class CreateLotRequest {
        @NotBlank private String bloodType;
        @NotNull private ComponentType componentType;
        @Min(1) @Max(10000) private int unitCount;
        @NotNull private LocalDate collectedOn;
        @NotNull private LocalDate expiresOn;
    }
    @Data
    public static class TemperatureRequest {
        @DecimalMin("-100") @DecimalMax("100") private double temperatureC;
        private boolean withinValidatedRange;
        @Size(max = 300) private String note;
    }
    @Data public static class HandoffRequest {
        @NotNull private UUID toHospitalId;
        @Min(1) private int unitCount;
        @Size(max = 300) private String reference;
    }
    public record TemperatureDto(UUID id, double temperatureC, boolean withinValidatedRange, String note, LocalDateTime recordedAt) {}
    public record HandoffDto(UUID id, UUID lotId, UUID fromHospitalId, UUID toHospitalId, int unitCount, String reference, String status, LocalDateTime recordedAt, LocalDateTime receivedAt) {}
    public record LotDto(UUID id, UUID hospitalId, String bloodType, ComponentType componentType, int unitCount,
                         LocalDate collectedOn, LocalDate expiresOn, LotStatus status) {}

    @PostMapping
    public ResponseEntity<LotDto> create(Authentication authentication, @PathVariable UUID hospitalId, @Valid @RequestBody CreateLotRequest dto) {
        accessService.assertCanAccess(authentication.getName(), hospitalId);
        if (dto.getExpiresOn().isBefore(dto.getCollectedOn())) return ResponseEntity.badRequest().build();
        Hospital hospital = hospitalRepository.findById(hospitalId).orElseThrow(() -> new IllegalArgumentException("Hospital not found"));
        BloodUnitLot lot = new BloodUnitLot();
        lot.setHospital(hospital);
        lot.setBloodType(dto.getBloodType().trim().toUpperCase());
        lot.setComponentType(dto.getComponentType());
        lot.setUnitCount(dto.getUnitCount());
        lot.setCollectedOn(dto.getCollectedOn());
        lot.setExpiresOn(dto.getExpiresOn());
        if (lot.getExpiresOn().isBefore(LocalDate.now())) lot.setStatus(LotStatus.EXPIRED);
        BloodUnitLot saved = lotRepository.save(lot);
        syncInventory(saved);
        return ResponseEntity.ok(toDto(saved));
    }

    @GetMapping
    public ResponseEntity<List<LotDto>> list(Authentication authentication, @PathVariable UUID hospitalId) {
        accessService.assertCanAccess(authentication.getName(), hospitalId);
        List<BloodUnitLot> lots = lotRepository.findByHospitalIdOrderByExpiresOnAsc(hospitalId);
        lots.stream().filter(l -> l.getStatus() == LotStatus.AVAILABLE && l.getExpiresOn().isBefore(LocalDate.now()))
                .forEach(l -> { l.setStatus(LotStatus.EXPIRED); lotRepository.save(l); syncInventory(l); });
        return ResponseEntity.ok(lots.stream().map(this::toDto).toList());
    }

    @PostMapping("/{lotId}/temperature")
    public ResponseEntity<TemperatureDto> recordTemperature(Authentication authentication, @PathVariable UUID hospitalId, @PathVariable UUID lotId,
            @Valid @RequestBody TemperatureRequest dto) {
        accessService.assertCanAccess(authentication.getName(), hospitalId);
        BloodUnitLot lot = lotRepository.findById(lotId).orElseThrow(() -> new IllegalArgumentException("Lot not found"));
        if (!lot.getHospital().getId().equals(hospitalId)) return ResponseEntity.notFound().build();
        BloodUnitTemperatureLog log = new BloodUnitTemperatureLog();
        log.setLot(lot);
        log.setTemperatureC(dto.getTemperatureC());
        log.setWithinValidatedRange(dto.isWithinValidatedRange());
        log.setNote(dto.getNote());
        if (!dto.isWithinValidatedRange() && lot.getStatus() == LotStatus.AVAILABLE) {
            lot.setStatus(LotStatus.QUARANTINED);
            lotRepository.save(lot);
            syncInventory(lot);
        }
        var saved = temperatureRepository.save(log);
        return ResponseEntity.ok(new TemperatureDto(saved.getId(), saved.getTemperatureC(), saved.isWithinValidatedRange(), saved.getNote(), saved.getRecordedAt()));
    }

    @GetMapping("/{lotId}/temperature")
    public ResponseEntity<List<TemperatureDto>> getTemperatureHistory(Authentication authentication, @PathVariable UUID hospitalId, @PathVariable UUID lotId) {
        accessService.assertCanAccess(authentication.getName(), hospitalId);
        BloodUnitLot lot = lotRepository.findById(lotId).orElseThrow(() -> new IllegalArgumentException("Lot not found"));
        if (!lot.getHospital().getId().equals(hospitalId)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(temperatureRepository.findByLotIdOrderByRecordedAtDesc(lotId).stream()
                .map(log -> new TemperatureDto(log.getId(), log.getTemperatureC(), log.isWithinValidatedRange(), log.getNote(), log.getRecordedAt())).toList());
    }

    @PostMapping("/{lotId}/handoffs")
    public ResponseEntity<HandoffDto> handoff(Authentication authentication, @PathVariable UUID hospitalId, @PathVariable UUID lotId,
            @Valid @RequestBody HandoffRequest dto) {
        accessService.assertCanAccess(authentication.getName(), hospitalId);
        BloodUnitLot lot = lotRepository.findById(lotId).orElseThrow(() -> new IllegalArgumentException("Lot not found"));
        if (!lot.getHospital().getId().equals(hospitalId)) return ResponseEntity.notFound().build();
        if (lot.getStatus() != LotStatus.AVAILABLE || dto.getUnitCount() > lot.getUnitCount()) return ResponseEntity.badRequest().build();
        Hospital destination = hospitalRepository.findById(dto.getToHospitalId()).orElseThrow(() -> new IllegalArgumentException("Destination hospital not found"));
        if (destination.getId().equals(hospitalId) || !Boolean.TRUE.equals(destination.getVerified())) return ResponseEntity.badRequest().build();
        BloodUnitHandoff handoff = new BloodUnitHandoff();
        handoff.setLot(lot);
        handoff.setFromHospital(lot.getHospital());
        handoff.setToHospital(destination);
        handoff.setUnitCount(dto.getUnitCount());
        handoff.setReference(dto.getReference());
        var saved = handoffRepository.save(handoff);
        saved.setStatus("IN_TRANSIT");
        if (dto.getUnitCount() == lot.getUnitCount()) lot.setStatus(LotStatus.TRANSFERRED);
        else lot.setUnitCount(lot.getUnitCount() - dto.getUnitCount());
        lotRepository.save(lot);
        syncInventory(lot);
        return ResponseEntity.ok(toHandoffDto(saved));
    }

    @PostMapping("/handoffs/{handoffId}/receive")
    public ResponseEntity<HandoffDto> receiveHandoff(Authentication authentication, @PathVariable UUID hospitalId, @PathVariable UUID handoffId) {
        accessService.assertCanAccess(authentication.getName(), hospitalId);
        BloodUnitHandoff handoff = handoffRepository.findById(handoffId).orElseThrow(() -> new IllegalArgumentException("Handoff not found"));
        if (!handoff.getToHospital().getId().equals(hospitalId)) return ResponseEntity.notFound().build();
        if (!"IN_TRANSIT".equals(handoff.getStatus())) return ResponseEntity.badRequest().build();
        if (!Boolean.TRUE.equals(handoff.getToHospital().getVerified())) return ResponseEntity.badRequest().build();
        BloodUnitLot source = handoff.getLot();
        BloodUnitLot receivedLot = new BloodUnitLot();
        receivedLot.setHospital(handoff.getToHospital());
        receivedLot.setBloodType(source.getBloodType());
        receivedLot.setComponentType(source.getComponentType());
        receivedLot.setUnitCount(handoff.getUnitCount());
        receivedLot.setCollectedOn(source.getCollectedOn());
        receivedLot.setExpiresOn(source.getExpiresOn());
        receivedLot.setSourceLotId(source.getSourceLotId() == null ? source.getId() : source.getSourceLotId());
        if (receivedLot.getExpiresOn().isBefore(LocalDate.now())) receivedLot.setStatus(LotStatus.EXPIRED);
        lotRepository.save(receivedLot);
        handoff.setStatus("RECEIVED");
        handoff.setReceivedAt(LocalDateTime.now());
        handoffRepository.save(handoff);
        syncInventory(receivedLot);
        return ResponseEntity.ok(toHandoffDto(handoff));
    }

    @GetMapping("/handoffs/incoming")
    public ResponseEntity<List<HandoffDto>> getIncomingHandoffs(Authentication authentication, @PathVariable UUID hospitalId) {
        accessService.assertCanAccess(authentication.getName(), hospitalId);
        return ResponseEntity.ok(handoffRepository.findByToHospitalIdAndStatusOrderByRecordedAtDesc(hospitalId, "IN_TRANSIT")
                .stream().map(this::toHandoffDto).toList());
    }

    private HandoffDto toHandoffDto(BloodUnitHandoff handoff) {
        return new HandoffDto(handoff.getId(), handoff.getLot().getId(), handoff.getFromHospital().getId(),
                handoff.getToHospital().getId(), handoff.getUnitCount(), handoff.getReference(), handoff.getStatus(),
                handoff.getRecordedAt(), handoff.getReceivedAt());
    }

    private void syncInventory(BloodUnitLot lot) {
        BloodInventory aggregate = inventoryRepository.findByHospitalIdAndBloodTypeAndComponentType(
                lot.getHospital().getId(), lot.getBloodType(), lot.getComponentType()).orElseGet(() -> {
                    BloodInventory fresh = new BloodInventory();
                    fresh.setHospital(lot.getHospital());
                    fresh.setBloodType(lot.getBloodType());
                    fresh.setComponentType(lot.getComponentType());
                    return fresh;
                });
        long units = lotRepository.sumActiveUnits(lot.getHospital().getId(), lot.getBloodType(), lot.getComponentType(), LotStatus.AVAILABLE);
        aggregate.setUnitsAvailable(Math.toIntExact(units + (aggregate.getUntrackedUnits() == null ? 0 : aggregate.getUntrackedUnits())));
        inventoryRepository.save(aggregate);
    }

    private LotDto toDto(BloodUnitLot lot) {
        return new LotDto(lot.getId(), lot.getHospital().getId(), lot.getBloodType(), lot.getComponentType(),
                lot.getUnitCount(), lot.getCollectedOn(), lot.getExpiresOn(), lot.getStatus());
    }
}
