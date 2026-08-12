package com.lifelink.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;

/**
 * Request DTO for updating a user's own profile.
 * Phone and role are intentionally excluded — they are immutable after registration.
 */
@Data
public class UpdateProfileDto {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Email(message = "Must be a valid email address")
    @Size(max = 150, message = "Email must be at most 150 characters")
    private String email;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must be at most 255 characters")
    private String address;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @Size(max = 20, message = "Gender must be at most 20 characters")
    private String gender;

    @Size(max = 50, message = "Emergency contact must be at most 50 characters")
    private String emergencyContact;

    private java.math.BigDecimal weight;

    private String medicalConditions;
}
