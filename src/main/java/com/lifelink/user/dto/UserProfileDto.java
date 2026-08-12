package com.lifelink.user.dto;

import com.lifelink.user.Role;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** Read-only view of the authenticated user's own profile. Never exposes passwordHash. */
@Data
public class UserProfileDto {
    private UUID id;
    private String name;
    private String phone;
    private String email;
    private Role role;
    private LocalDateTime createdAt;
    private String address;
    private LocalDate dateOfBirth;
    private String gender;
    private String emergencyContact;
    private Double weight;
    private String medicalConditions;
}
