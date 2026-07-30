package com.lifelink.request.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmergencyRequestShareDto {
    private String id;
    private String bloodType;
    private String componentType;
    private String urgency;
    private String status;
    private double latitude;
    private double longitude;
}
