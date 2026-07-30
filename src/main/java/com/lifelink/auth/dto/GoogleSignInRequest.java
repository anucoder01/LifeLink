package com.lifelink.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleSignInRequest {
    @NotBlank
    private String idToken; // Firebase Auth ID Token
}
