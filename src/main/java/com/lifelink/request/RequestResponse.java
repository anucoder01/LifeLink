package com.lifelink.request;

import com.lifelink.donor.Donor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "request_responses")
@Getter
@Setter
public class RequestResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", referencedColumnName = "id")
    private EmergencyRequest request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", referencedColumnName = "id")
    private Donor donor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestResponseStatus status;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
}
