package com.lifelink.hospital.dto;

import com.lifelink.request.ComponentType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** Request DTO for setting or updating blood inventory stock. */
@Data
public class UpdateInventoryDto {

    @NotBlank(message = "Blood type is required")
    @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "Invalid blood type (e.g. A+, O-, AB+)")
    private String bloodType;

    @NotNull(message = "Component type is required")
    private ComponentType componentType;

    @Min(value = 0, message = "Units available cannot be negative")
    private int unitsAvailable;
}
