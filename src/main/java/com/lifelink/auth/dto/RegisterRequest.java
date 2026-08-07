package com.lifelink.auth.dto;

import com.lifelink.user.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String phone;
    private String email;
    private String password;
    private Role role;
    // For Donors:
    private String bloodType;
    private Double latitude;
    private Double longitude;
    
    // For Institutions (Blood Bank / NGO):
    private String institutionName;
    private String address;
    private String contactPhone;
    private String licenseOrRegistrationNumber;
    private String operatingHours;
    private String focusAreas;
}
