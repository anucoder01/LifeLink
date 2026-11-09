package com.lifelink.hospital;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class BloodUnitLotExpiryJob {
    private final BloodUnitLotRepository lotRepository;
    private final BloodInventoryRepository inventoryRepository;

    @Scheduled(cron = "0 10 0 * * *")
    @Transactional
    public void expireLots() {
        var expired = lotRepository.findByStatusAndExpiresOnBefore(LotStatus.AVAILABLE, LocalDate.now());
        expired.forEach(lot -> lot.setStatus(LotStatus.EXPIRED));
        lotRepository.saveAll(expired);
        expired.stream().map(lot -> lot.getHospital().getId() + "|" + lot.getBloodType() + "|" + lot.getComponentType())
                .distinct().forEach(key -> {
                    String[] parts = key.split("\\|");
                    var matchingLots = expired.stream().filter(l -> l.getHospital().getId().toString().equals(parts[0])
                            && l.getBloodType().equals(parts[1]) && l.getComponentType().name().equals(parts[2])).findFirst().orElseThrow();
                    long activeUnits = lotRepository.sumActiveUnits(matchingLots.getHospital().getId(), matchingLots.getBloodType(),
                            matchingLots.getComponentType(), LotStatus.AVAILABLE);
                    inventoryRepository.findByHospitalIdAndBloodTypeAndComponentType(matchingLots.getHospital().getId(),
                            matchingLots.getBloodType(), matchingLots.getComponentType()).ifPresent(inventory -> {
                                inventory.setUnitsAvailable(Math.toIntExact(activeUnits + (inventory.getUntrackedUnits() == null ? 0 : inventory.getUntrackedUnits())));
                                inventoryRepository.save(inventory);
                            });
                });
        if (!expired.isEmpty()) log.info("Expired {} blood unit lots and refreshed inventory totals", expired.size());
    }
}
