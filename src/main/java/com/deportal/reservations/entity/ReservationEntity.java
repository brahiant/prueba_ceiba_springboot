package com.deportal.reservations.entity;

import com.deportal.courts.entity.CourtEntity;
import com.deportal.reservations.enums.ReservationStatus;
import com.deportal.users.entity.UserEntity;
import com.deportal.users.enums.CustomerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "reservations")
public class ReservationEntity {

    @Id
    @Column(name = "reservation_id", nullable = false, updatable = false, length = 36)
    private String reservationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "court_id", nullable = false)
    private CourtEntity court;

    @Column(name = "customer_name", nullable = false, length = 120)
    private String customerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", nullable = false, length = 20)
    private CustomerType customerType;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "duration_hours", nullable = false)
    private int durationHours;

    @Column(name = "base_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseAmount;

    @Column(name = "member_discount", nullable = false, precision = 10, scale = 2)
    private BigDecimal memberDiscount;

    @Column(name = "off_peak_discount", nullable = false, precision = 10, scale = 2)
    private BigDecimal offPeakDiscount;

    @Column(name = "total_discount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalDiscount;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    protected ReservationEntity() {
    }

    public ReservationEntity(
            UserEntity user,
            CourtEntity court,
            String customerName,
            CustomerType customerType,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            int durationHours,
            BigDecimal baseAmount,
            BigDecimal memberDiscount,
            BigDecimal offPeakDiscount,
            BigDecimal totalDiscount,
            BigDecimal totalAmount,
            ReservationStatus status) {
        this.user = user;
        this.court = court;
        this.customerName = customerName;
        this.customerType = customerType;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationHours = durationHours;
        this.baseAmount = baseAmount;
        this.memberDiscount = memberDiscount;
        this.offPeakDiscount = offPeakDiscount;
        this.totalDiscount = totalDiscount;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.reservationId = this.reservationId == null ? UUID.randomUUID().toString() : this.reservationId;
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public String getReservationId() {
        return reservationId;
    }

    public UserEntity getUser() {
        return user;
    }

    public CourtEntity getCourt() {
        return court;
    }

    public String getCustomerName() {
        return customerName;
    }

    public CustomerType getCustomerType() {
        return customerType;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public int getDurationHours() {
        return durationHours;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public BigDecimal getMemberDiscount() {
        return memberDiscount;
    }

    public BigDecimal getOffPeakDiscount() {
        return offPeakDiscount;
    }

    public BigDecimal getTotalDiscount() {
        return totalDiscount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}
