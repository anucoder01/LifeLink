package com.lifelink.ngo;

import com.lifelink.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ngos")
@Getter
@Setter
public class Ngo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "geography(Point,4326)")
    private Point location; // NGOs might just have an office location

    @Column(name = "verified")
    private Boolean verified = false;

    @Column(name = "address", length = 300)
    private String address;

    @Column(name = "contact_phone", length = 15)
    private String contactPhone;

    @Column(name = "registration_number", length = 50)
    private String registrationNumber;

    @Column(name = "focus_areas", length = 200)
    private String focusAreas;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
