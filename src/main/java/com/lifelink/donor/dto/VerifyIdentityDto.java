package com.lifelink.donor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyIdentityDto {
    @NotBlank
    private String governmentId; // e.g., ABHA ID, Aadhaar (we will only store the hash)
}
