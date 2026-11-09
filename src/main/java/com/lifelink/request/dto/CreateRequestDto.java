package com.lifelink.request.dto;

import com.lifelink.request.ComponentType;
import com.lifelink.request.Urgency;
import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class CreateRequestDto {
    @NotBlank private String bloodType;
    @NotNull private ComponentType componentType;
    @NotNull private Urgency urgency;
    @NotNull @DecimalMin("-90.0") @DecimalMax("90.0")
    private Double latitude;
    @NotNull @DecimalMin("-180.0") @DecimalMax("180.0")
    private Double longitude;
    @Size(max = 500) private String verificationReference;
    @Min(1) @Max(10) private Integer unitsRequired = 1;
    @Size(max = 150) private String facilityName;
}
