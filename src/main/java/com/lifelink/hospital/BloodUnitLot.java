package com.lifelink.hospital;

import com.lifelink.request.ComponentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "blood_unit_lots")
@Getter @Setter
public class BloodUnitLot {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;
    @Column(name = "blood_type", nullable = false, length = 5)
    private String bloodType;
    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", nullable = false, length = 20)
    private ComponentType componentType;
    @Column(name = "unit_count", nullable = false)
    private int unitCount;
    @Column(name = "source_lot_id")
    private UUID sourceLotId;
    @Column(name = "collected_on", nullable = false)
    private LocalDate collectedOn;
    @Column(name = "expires_on", nullable = false)
    private LocalDate expiresOn;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LotStatus status = LotStatus.AVAILABLE;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
