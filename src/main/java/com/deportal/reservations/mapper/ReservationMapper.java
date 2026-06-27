package com.deportal.reservations.mapper;

import com.deportal.reservations.dto.CancelReservationResponse;
import com.deportal.reservations.dto.ReservationResponse;
import com.deportal.reservations.entity.ReservationEntity;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

    public ReservationResponse toResponse(ReservationEntity entity) {
        return new ReservationResponse(
                entity.getReservationId(),
                entity.getUser().getUserId(),
                entity.getCustomerName(),
                entity.getCustomerType(),
                entity.getCourt().getCourtId(),
                entity.getCourt().getName(),
                entity.getDate(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getDurationHours(),
                entity.getBaseAmount(),
                entity.getMemberDiscount(),
                entity.getOffPeakDiscount(),
                entity.getTotalDiscount(),
                entity.getTotalAmount(),
                entity.getRefundAmount(),
                entity.getStatus());
    }

    public CancelReservationResponse toCancelResponse(ReservationEntity entity) {
        return new CancelReservationResponse(
                entity.getReservationId(),
                entity.getStatus(),
                entity.getRefundAmount());
    }
}
