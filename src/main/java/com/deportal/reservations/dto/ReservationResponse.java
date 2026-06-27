package com.deportal.reservations.dto;

import com.deportal.reservations.enums.ReservationStatus;
import com.deportal.users.enums.CustomerType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationResponse(
        String reservationId,
        String userId,
        String customerName,
        CustomerType customerType,
        String courtId,
        String courtName,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        int durationHours,
        BigDecimal baseAmount,
        BigDecimal memberDiscount,
        BigDecimal offPeakDiscount,
        BigDecimal totalDiscount,
        BigDecimal totalAmount,
        BigDecimal refundAmount,
        ReservationStatus status) {
}
