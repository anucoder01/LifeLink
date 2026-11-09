package com.lifelink.request;

import com.lifelink.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "emergency_requests")
@Getter
@Setter
public class EmergencyRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", referencedColumnName = "id")
    private User requester;

    @Column(name = "blood_type", nullable = false, length = 5)
    private String bloodType;

    @Column(name = "units_required", nullable = false)
    private int unitsRequired = 1;

    @Column(name = "facility_name", length = 150)
    private String facilityName;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", nullable = false, length = 20)
    private ComponentType componentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Urgency urgency;

    @Column(columnDefinition = "geography(Point,4326)", nullable = false)
    private Point location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "current_radius_km", nullable = false)
    private int currentRadiusKm = 5;

    @Column(name = "disaster_mode", nullable = false)
    private boolean disasterMode = false;

    @Column(name = "verified", nullable = false)
    private boolean verified = true;

    @Column(name = "verification_reference", length = 500)
    private String verificationReference;

    @Column(name = "verified_by")
    private UUID verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}
