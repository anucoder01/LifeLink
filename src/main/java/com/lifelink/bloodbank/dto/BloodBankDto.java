package com.lifelink.bloodbank.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BloodBankDto {
    private UUID id;
    private String name;
    private Double latitude;
    private Double longitude;
    private Boolean verified;
    private String address;
    private String contactPhone;
    private String licenseNumber;
    private String operatingHours;
    private LocalDateTime createdAt;
}
