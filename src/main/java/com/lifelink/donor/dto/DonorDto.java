package com.lifelink.donor.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DonorDto {
    private String id;
    private String name;
    private String bloodType;
    private Double latitude;
    private Double longitude;
    private LocalDate lastDonationDate;
    private Boolean isActive;
    // Anonymized contact exposure: phone is intentionally omitted.
}
