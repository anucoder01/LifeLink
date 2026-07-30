package com.lifelink.donor;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "donor_health_questionnaires")
@Getter
@Setter
public class DonorHealthQuestionnaire {

    @Id
    private UUID donorId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "donor_id")
    private Donor donor;

    @Column(name = "had_recent_illness")
    private Boolean hadRecentIllness = false;

    @Column(name = "had_recent_surgery")
    private Boolean hadRecentSurgery = false;

    @Column(name = "on_medication")
    private Boolean onMedication = false;

    @Column(name = "is_pregnant")
    private Boolean isPregnant = false;

    @Column(name = "has_recent_tattoos")
    private Boolean hasRecentTattoos = false;

    @Column(name = "consumed_alcohol_recently")
    private Boolean consumedAlcoholRecently = false;

    @UpdateTimestamp
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}
