package com.lifelink.hospital;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "hospital_transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requesting_hospital_id", nullable = false)
    private Hospital requestingHospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "providing_hospital_id")
    private Hospital providingHospital;

    @Column(name = "blood_type", nullable = false, length = 5)
    private String bloodType;

    @Column(name = "component_type", nullable = false, length = 20)
    private String componentType;

    @Column(name = "units_requested", nullable = false)
    private Integer unitsRequested;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String urgency = "NORMAL"; // CRITICAL, HIGH, NORMAL

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "REQUESTED"; // REQUESTED, ACCEPTED, IN_TRANSIT, COMPLETED, REJECTED, CANCELLED

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
