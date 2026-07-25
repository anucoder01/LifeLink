package com.lifelink.hospital.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/** Response DTO for a hospital record. */
@Data
public class HospitalDto {
    private UUID id;
    private String name;
    private double latitude;
    private double longitude;
    private Boolean verified;
    private String address;
    private String contactPhone;
    private LocalDateTime createdAt;
}
