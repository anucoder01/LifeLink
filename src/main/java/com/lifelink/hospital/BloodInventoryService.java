package com.lifelink.hospital;

import com.lifelink.config.CacheNames;
import com.lifelink.hospital.dto.InventoryDto;
import com.lifelink.hospital.dto.UpdateInventoryDto;
import com.lifelink.request.ComponentType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Slf4j
@Service
@RequiredArgsConstructor
public class BloodInventoryService {

    private final BloodInventoryRepository inventoryRepository;
    private final HospitalRepository hospitalRepository;

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /**
     * Returns the paginated inventory for a hospital.
     */
    @Transactional(readOnly = true)
    public Page<InventoryDto> getByHospital(UUID hospitalId, Pageable pageable) {
        assertHospitalExists(hospitalId);
        return inventoryRepository.findByHospitalId(hospitalId, pageable)
                .map(this::mapToDto);
    }

    /**
     * Returns total units available across all verified hospitals for a given
     * blood type and component type.
     */
    @Transactional(readOnly = true)
    public long getTotalAvailable(String bloodType, ComponentType componentType) {
        return inventoryRepository
                .sumAvailableUnits(bloodType.toUpperCase(), componentType)
                .orElse(0L);
    }

    // -------------------------------------------------------------------------
    // Mutations
    // -------------------------------------------------------------------------

    /**
     * Upserts the inventory record for a hospital + blood type + component.
     * If the record doesn't exist yet, it is created.
     * Called by hospital admins to update their current stock.
     * Evicts the nearby-blood cache since availability has changed.
     */
    @Caching(evict = {
        @CacheEvict(value = CacheNames.HOSPITALS_NEARBY_BLOOD, allEntries = true)
    })
    @Transactional
    public InventoryDto upsert(UUID hospitalId, UpdateInventoryDto dto) {
        Hospital hospital = findHospitalOrThrow(hospitalId);

        BloodInventory inventory = inventoryRepository
                .findByHospitalIdAndBloodTypeAndComponentType(
                        hospitalId,
                        dto.getBloodType().toUpperCase(),
                        dto.getComponentType())
                .orElseGet(() -> {
                    BloodInventory fresh = new BloodInventory();
                    fresh.setHospital(hospital);
                    fresh.setBloodType(dto.getBloodType().toUpperCase());
                    fresh.setComponentType(dto.getComponentType());
                    return fresh;
                });

        inventory.setUnitsAvailable(dto.getUnitsAvailable());
        inventory = inventoryRepository.save(inventory);
        log.info("Inventory upserted — hospital={} bloodType={} component={} units={}",
                hospitalId, dto.getBloodType(), dto.getComponentType(), dto.getUnitsAvailable());

        return mapToDto(inventory);
    }

    /**
     * Deletes a single inventory record by its own ID.
     * Evicts the nearby-blood cache since availability has changed.
     */
    @CacheEvict(value = CacheNames.HOSPITALS_NEARBY_BLOOD, allEntries = true)
    @Transactional
    public void delete(UUID inventoryId) {
        if (!inventoryRepository.existsById(inventoryId)) {
            throw new EntityNotFoundException("Inventory record not found: " + inventoryId);
        }
        inventoryRepository.deleteById(inventoryId);
        log.info("Deleted inventory record {}", inventoryId);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private Hospital findHospitalOrThrow(UUID hospitalId) {
        return hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new EntityNotFoundException("Hospital not found: " + hospitalId));
    }

    private void assertHospitalExists(UUID hospitalId) {
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new EntityNotFoundException("Hospital not found: " + hospitalId);
        }
    }

    public InventoryDto mapToDto(BloodInventory inv) {
        InventoryDto dto = new InventoryDto();
        dto.setId(inv.getId());
        dto.setHospitalId(inv.getHospital().getId());
        dto.setHospitalName(inv.getHospital().getName());
        dto.setBloodType(inv.getBloodType());
        dto.setComponentType(inv.getComponentType());
        dto.setUnitsAvailable(inv.getUnitsAvailable());
        dto.setUpdatedAt(inv.getUpdatedAt());
        return dto;
    }
}
