package com.lifelink.driver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DriverRegistrationDto {
    @NotNull
    private Double latitude;
    
    @NotNull
    private Double longitude;
    
    @NotBlank
    private String vehicleType;
}
