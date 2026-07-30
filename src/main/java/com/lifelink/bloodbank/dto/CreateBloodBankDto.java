package com.lifelink.bloodbank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateBloodBankDto {
    @NotBlank
    private String name;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    private String address;
    private String contactPhone;
    private String licenseNumber;
    private String operatingHours;
}
