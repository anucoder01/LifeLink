package com.lifelink.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpSendRequest {
    @NotBlank
    private String phone;
}
