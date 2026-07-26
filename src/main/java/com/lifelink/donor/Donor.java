package com.lifelink.donor;

import com.lifelink.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "donors")
@Getter
@Setter
public class Donor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "blood_type", nullable = false, length = 5)
    private String bloodType;

    @Column(columnDefinition = "geography(Point,4326)", nullable = false)
    private Point location;

    @Column(name = "last_donation_date")
    private LocalDate lastDonationDate;

    @Column(name = "reliability_score", precision = 5, scale = 2)
    private BigDecimal reliabilityScore = new BigDecimal("100.00");

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "fcm_token")
    private String fcmToken;
}
