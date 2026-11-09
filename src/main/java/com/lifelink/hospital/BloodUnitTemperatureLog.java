package com.lifelink.hospital;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "blood_unit_temperature_logs")
@Getter @Setter
public class BloodUnitTemperatureLog {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lot_id", nullable = false)
    private BloodUnitLot lot;
    @Column(name = "temperature_c", nullable = false)
    private double temperatureC;
    @Column(name = "within_validated_range", nullable = false)
    private boolean withinValidatedRange;
    @Column(length = 300)
    private String note;
    @CreationTimestamp
    @Column(name = "recorded_at", updatable = false)
    private LocalDateTime recordedAt;
}
