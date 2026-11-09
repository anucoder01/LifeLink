package com.lifelink.donor;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "donor_recovery_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonorRecoveryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    private Donor donor;

    @Column(name = "donation_date", nullable = false)
    private LocalDate donationDate;

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    @Column(name = "water_ml_logged")
    @Builder.Default
    private Integer waterMlLogged = 0;

    @Column(name = "feeling_status", length = 30)
    @Builder.Default
    private String feelingStatus = "GOOD"; // EXCELLENT, GOOD, TIRED, DIZZY, COMPLICATION

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "logged_at")
    @Builder.Default
    private LocalDateTime loggedAt = LocalDateTime.now();
}
