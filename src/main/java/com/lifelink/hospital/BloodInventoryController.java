package com.lifelink.hospital;

import com.lifelink.hospital.dto.InventoryDto;
import com.lifelink.hospital.dto.UpdateInventoryDto;
import com.lifelink.request.ComponentType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import com.lifelink.common.dto.PaginatedResponse;
import com.lifelink.common.util.PaginationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Tag(name = "Blood Inventory", description = "Hospital blood stock management and availability queries")
@RestController
@RequestMapping("/api/v1/hospitals")
@RequiredArgsConstructor
public class BloodInventoryController {

    private final BloodInventoryService inventoryService;

    // -------------------------------------------------------------------------
    // Per-hospital stock
    // -------------------------------------------------------------------------

    /**
     * GET /api/v1/hospitals/{hospitalId}/inventory
     * Returns all inventory records for the given hospital with pagination.
     */
    @Operation(summary = "Get blood inventory for a hospital with pagination")
    @GetMapping("/{hospitalId}/inventory")
    public ResponseEntity<PaginatedResponse<InventoryDto>> getByHospital(
            @PathVariable UUID hospitalId,
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        Page<InventoryDto> page = inventoryService.getByHospital(hospitalId, pageable);
        return ResponseEntity.ok(PaginationUtil.fromPage(page));
    }

    /**
     * PUT /api/v1/hospitals/{hospitalId}/inventory
     * Creates or updates a blood inventory record.
     * If a record for the same bloodType + componentType already exists it is updated;
     * otherwise a new record is inserted (upsert semantics).
     */
    @Operation(summary = "Upsert blood inventory stock for a hospital")
    @PutMapping("/{hospitalId}/inventory")
    public ResponseEntity<InventoryDto> upsert(
            @PathVariable UUID hospitalId,
            @Valid @RequestBody UpdateInventoryDto dto) {
        return ResponseEntity.ok(inventoryService.upsert(hospitalId, dto));
    }

    /**
     * DELETE /api/v1/hospitals/{hospitalId}/inventory/{inventoryId}
     * Removes a specific inventory record.
     */
    @Operation(summary = "Remove a blood inventory record")
    @DeleteMapping("/{hospitalId}/inventory/{inventoryId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID hospitalId,
            @PathVariable UUID inventoryId) {
        inventoryService.delete(inventoryId);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // System-wide availability
    // -------------------------------------------------------------------------

    /**
     * GET /api/v1/hospitals/inventory/availability?bloodType=A%2B&componentType=WHOLE_BLOOD
     * Returns the total units available system-wide for a given blood type + component
     * across all verified hospitals.
     */
    @Operation(summary = "Get system-wide available units for a blood type and component")
    @GetMapping("/inventory/availability")
    public ResponseEntity<Long> getSystemAvailability(
            @RequestParam String bloodType,
            @RequestParam ComponentType componentType) {
        return ResponseEntity.ok(inventoryService.getTotalAvailable(bloodType, componentType));
    }
}
