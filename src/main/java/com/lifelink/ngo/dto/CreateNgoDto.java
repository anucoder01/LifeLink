package com.lifelink.ngo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateNgoDto {
    @NotBlank
    private String name;

    private Double latitude; // optional for NGOs
    private Double longitude; // optional for NGOs

    private String address;
    private String contactPhone;
    private String registrationNumber;
    private String focusAreas;
}
