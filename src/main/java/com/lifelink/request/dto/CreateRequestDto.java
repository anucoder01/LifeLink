package com.lifelink.request.dto;

import com.lifelink.request.ComponentType;
import com.lifelink.request.Urgency;
import lombok.Data;

@Data
public class CreateRequestDto {
    private String bloodType;
    private ComponentType componentType;
    private Urgency urgency;
    private Double latitude;
    private Double longitude;
}
