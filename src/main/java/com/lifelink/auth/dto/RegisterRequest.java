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
}
