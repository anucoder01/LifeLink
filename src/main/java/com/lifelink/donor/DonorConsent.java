package com.lifelink.donor;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "donor_consents")
@Getter
@Setter
public class DonorConsent {

    @Id
    private UUID donorId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "donor_id")
    private Donor donor;

    @Column(name = "share_location")
    private Boolean shareLocation = true;

    @Column(name = "allow_emergency_notifications")
    private Boolean allowEmergencyNotifications = true;

    @Column(name = "allow_camp_notifications")
    private Boolean allowCampNotifications = true;

    @Column(name = "show_name_on_public_impact_board")
    private Boolean showNameOnPublicImpactBoard = false;

    @UpdateTimestamp
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}
