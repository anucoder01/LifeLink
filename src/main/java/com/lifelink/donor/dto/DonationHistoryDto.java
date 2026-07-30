package com.lifelink.donor.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DonationHistoryDto {
    private String requestId;
    private String hospitalOrRequesterName;
    private String componentType;
    private LocalDateTime donatedAt;
}
