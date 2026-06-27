package com.deportal.courts.entity;

import com.deportal.courts.enums.SportType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "courts")
public class CourtEntity {

    @Id
    @Column(name = "court_id", nullable = false, updatable = false, length = 36)
    private String courtId;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "sport_type", nullable = false, length = 20)
    private SportType sportType;

    @Column(nullable = false)
    private int capacity;

    @Column(name = "opening_time", nullable = false)
    private LocalTime openingTime;

    @Column(name = "closing_time", nullable = false)
    private LocalTime closingTime;

    @Column(name = "hourly_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CourtEntity() {
    }

    public CourtEntity(String name, SportType sportType, int capacity, LocalTime openingTime, LocalTime closingTime, BigDecimal hourlyRate, boolean active) {
        this.name = name;
        this.sportType = sportType;
        this.capacity = capacity;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.hourlyRate = hourlyRate;
        this.active = active;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.courtId = this.courtId == null ? UUID.randomUUID().toString() : this.courtId;
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public String getCourtId() {
        return courtId;
    }

    public String getName() {
        return name;
    }

    public SportType getSportType() {
        return sportType;
    }

    public int getCapacity() {
        return capacity;
    }

    public LocalTime getOpeningTime() {
        return openingTime;
    }

    public LocalTime getClosingTime() {
        return closingTime;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
