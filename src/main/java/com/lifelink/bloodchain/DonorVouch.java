package com.lifelink.bloodchain;

import com.lifelink.donor.Donor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a trusted contact nominated by a registered donor.
 * The contact is a non-registered person the donor trusts.
 * Their data is held here only until they register; if they opt-out, this row is deleted.
 */
@Entity
@Table(name = "donor_vouches",
        uniqueConstraints = @UniqueConstraint(columnNames = {"donor_id", "contact_phone"}))
@Getter
@Setter
public class DonorVouch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    private Donor donor;

    @Column(name = "contact_phone", nullable = false, length = 15)
    private String contactPhone;

    @Column(name = "contact_name", nullable = false, length = 100)
    private String contactName;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
