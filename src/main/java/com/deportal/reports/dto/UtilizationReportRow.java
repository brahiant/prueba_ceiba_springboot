package com.deportal.reports.dto;

import java.math.BigDecimal;

public record UtilizationReportRow(
        String courtId,
        String courtName,
        long totalReservations,
        int reservedHours,
        int availableHours,
        BigDecimal totalIncome,
        BigDecimal occupancyRate) {
}
