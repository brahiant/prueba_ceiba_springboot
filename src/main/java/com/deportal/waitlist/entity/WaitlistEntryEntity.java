package com.deportal.waitlist.entity;

import com.deportal.courts.entity.CourtEntity;
import com.deportal.reservations.entity.ReservationEntity;
import com.deportal.users.entity.UserEntity;
import com.deportal.waitlist.enums.WaitlistStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "waitlist_entries")
public class WaitlistEntryEntity {

    @Id
    @Column(name = "waitlist_id", nullable = false, updatable = false, length = 36)
    private String waitlistId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private ReservationEntity reservation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "court_id", nullable = false)
    private CourtEntity court;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "duration_hours", nullable = false)
    private int durationHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WaitlistStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected WaitlistEntryEntity() {
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.waitlistId = this.waitlistId == null ? UUID.randomUUID().toString() : this.waitlistId;
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public String getWaitlistId() {
        return waitlistId;
    }
}
