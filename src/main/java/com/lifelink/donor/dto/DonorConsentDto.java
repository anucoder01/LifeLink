package com.lifelink.donor.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DonorConsentDto {
    private Boolean shareLocation;
    private Boolean allowEmergencyNotifications;
    private Boolean allowCampNotifications;
    private Boolean showNameOnPublicImpactBoard;
    private LocalDateTime lastUpdated;
}
