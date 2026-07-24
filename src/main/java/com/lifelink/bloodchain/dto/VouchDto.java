package com.lifelink.bloodchain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class VouchDto {
    private UUID id;
    private String contactName;
    /** Partially masked for display: e.g. "98765****10" */
    private String contactPhoneMasked;
    private LocalDateTime createdAt;
}
