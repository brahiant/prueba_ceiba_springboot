package com.deportal.reservations.dto;

import com.deportal.reservations.enums.ReservationStatus;
import java.math.BigDecimal;

public record CancelReservationResponse(
        String reservationId,
        ReservationStatus status,
        BigDecimal refundAmount) {
}
