package com.deportal.reservations.dto;

import com.deportal.users.enums.CustomerType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record CreateReservationRequest(
        @NotBlank(message = "El usuario es obligatorio")
        String userId,

        @NotBlank(message = "La cancha es obligatoria")
        String courtId,

        @NotNull(message = "La fecha de reserva es obligatoria")
        LocalDate date,

        @NotNull(message = "La hora de inicio es obligatoria")
        LocalTime startTime,

        @Min(value = 1, message = "La duracion minima es 1 hora")
        @Max(value = 8, message = "La duracion maxima es 8 horas")
        int durationHours,

        @NotNull(message = "El tipo de cliente es obligatorio")
        CustomerType customerType) {
}
