package com.lifelink.donor;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "donor_badges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonorBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    private Donor donor;

    @Column(name = "badge_key", nullable = false, length = 50)
    private String badgeKey;

    @Column(name = "badge_name", nullable = false, length = 100)
    private String badgeName;

    @Column(name = "badge_icon", nullable = false, length = 50)
    private String badgeIcon;

    @Column(name = "earned_at")
    @Builder.Default
    private LocalDateTime earnedAt = LocalDateTime.now();
}
