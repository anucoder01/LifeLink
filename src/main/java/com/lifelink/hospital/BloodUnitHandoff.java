package com.lifelink.hospital;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "blood_unit_handoffs")
@Getter @Setter
public class BloodUnitHandoff {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lot_id", nullable = false)
    private BloodUnitLot lot;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_hospital_id", nullable = false)
    private Hospital fromHospital;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_hospital_id", nullable = false)
    private Hospital toHospital;
    @Column(name = "unit_count", nullable = false)
    private int unitCount;
    @Column(length = 300)
    private String reference;
    @Column(nullable = false, length = 20)
    private String status = "IN_TRANSIT";
    @Column(name = "received_at")
    private LocalDateTime receivedAt;
    @CreationTimestamp
    @Column(name = "recorded_at", updatable = false)
    private LocalDateTime recordedAt;
}
