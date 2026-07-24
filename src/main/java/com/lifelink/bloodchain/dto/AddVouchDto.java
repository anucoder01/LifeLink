package com.lifelink.bloodchain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddVouchDto {

    @NotBlank(message = "Contact name is required")
    @Size(max = 100, message = "Name must be 100 characters or fewer")
    private String contactName;

    @NotBlank(message = "Contact phone is required")
    @Pattern(regexp = "^[0-9+\\-\\s]{7,15}$", message = "Invalid phone number format")
    private String contactPhone;
}
