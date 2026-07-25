package com.lifelink.hospital.dto;

import com.lifelink.request.ComponentType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/** Response DTO for a blood inventory record. */
@Data
public class InventoryDto {
    private UUID id;
    private UUID hospitalId;
    private String hospitalName;
    private String bloodType;
    private ComponentType componentType;
    private int unitsAvailable;
    private LocalDateTime updatedAt;
}
