package com.lifelink.bloodchain;

import com.lifelink.request.EmergencyRequest;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A single-use, time-limited token sent via SMS to a vouched contact.
 * When the contact taps the link and registers, the token is marked used and the row is retained for audit.
 * A scheduled job cleans up expired+unused rows after 7 days.
 */
@Entity
@Table(name = "blood_chain_invite_tokens")
@Getter
@Setter
public class BloodChainInviteToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Cryptographically random 64-char hex string used in the invite URL. */
    @Column(nullable = false, unique = true, length = 64)
    private String token;

    /** The emergency request that triggered this invite. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private EmergencyRequest request;

    /** Phone number the SMS was sent to. Pre-fills the registration form. */
    @Column(name = "contact_phone", nullable = false, length = 15)
    private String contactPhone;

    /** Name the donor used when nominating this contact. Pre-fills registration. */
    @Column(name = "contact_name", nullable = false, length = 100)
    private String contactName;

    /** Token expires 72 hours after creation. */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** True once the contact has clicked the link and registered. */
    @Column(nullable = false)
    private Boolean used = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
