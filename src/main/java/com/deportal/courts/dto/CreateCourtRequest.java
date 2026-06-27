package com.deportal.courts.dto;

import com.deportal.courts.enums.SportType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalTime;

public record CreateCourtRequest(
        @NotBlank(message = "El nombre de la cancha es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
        String name,

        @NotNull(message = "El tipo de deporte es obligatorio")
        SportType sportType,

        @Min(value = 1, message = "La capacidad minima es 1")
        @Max(value = 50, message = "La capacidad maxima es 50")
        int capacity,

        @NotNull(message = "La hora de apertura es obligatoria")
        LocalTime openingTime,

        @NotNull(message = "La hora de cierre es obligatoria")
        LocalTime closingTime,

        @NotNull(message = "La tarifa por hora es obligatoria")
        @DecimalMin(value = "5.00", message = "La tarifa por hora debe ser mayor o igual a 5.00")
        BigDecimal hourlyRate) {
}
