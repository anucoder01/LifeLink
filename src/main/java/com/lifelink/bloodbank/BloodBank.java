package com.lifelink.bloodbank;

import com.lifelink.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "blood_banks")
@Getter
@Setter
public class BloodBank {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "geography(Point,4326)", nullable = false)
    private Point location;

    @Column(name = "verified")
    private Boolean verified = false;

    @Column(name = "address", length = 300)
    private String address;

    @Column(name = "contact_phone", length = 15)
    private String contactPhone;

    @Column(name = "license_number", length = 50)
    private String licenseNumber;

    @Column(name = "operating_hours", length = 100)
    private String operatingHours;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
