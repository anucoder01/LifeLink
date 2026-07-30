package com.lifelink.ngo.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class NgoDto {
    private UUID id;
    private String name;
    private Double latitude;
    private Double longitude;
    private Boolean verified;
    private String address;
    private String contactPhone;
    private String registrationNumber;
    private String focusAreas;
    private LocalDateTime createdAt;
}
