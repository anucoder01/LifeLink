package com.lifelink.donor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FcmTokenDto {

    @NotBlank(message = "FCM token must not be blank")
    private String fcmToken;
}
