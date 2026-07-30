package com.lifelink.notification;

import com.lifelink.donor.Donor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false, unique = true)
    private Donor donor;

    @Column(name = "sms_enabled", nullable = false)
    private Boolean smsEnabled = true;

    @Column(name = "whatsapp_enabled", nullable = false)
    private Boolean whatsappEnabled = false;

    @Column(name = "silent_start_time")
    private LocalTime silentStartTime;

    @Column(name = "silent_end_time")
    private LocalTime silentEndTime;

    @Column(name = "sms_opted_out", nullable = false)
    private Boolean smsOptedOut = false;
}
