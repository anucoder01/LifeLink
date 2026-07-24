package com.lifelink.bloodchain.dto;

import lombok.Data;

/**
 * Returned when a contact opens their invite link.
 * Pre-fills the registration form with name and phone number (no password or blood type yet).
 */
@Data
public class InviteDetailsDto {
    private String contactName;
    private String contactPhone;
    /** A summary of the emergency that triggered this invite — no PII. */
    private String emergencySummary;
    private boolean valid;
    private String invalidReason;
}
