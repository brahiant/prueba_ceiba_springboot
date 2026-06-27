package com.deportal.courts.dto;

import com.deportal.courts.enums.SportType;
import java.math.BigDecimal;
import java.time.LocalTime;

public record CourtResponse(
        String courtId,
        String name,
        SportType sportType,
        int capacity,
        LocalTime openingTime,
        LocalTime closingTime,
        BigDecimal hourlyRate,
        boolean active) {
}
