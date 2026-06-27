package com.deportal.reports.dto;

import java.time.LocalDate;
import java.util.List;

public record UtilizationReportResponse(
        LocalDate from,
        LocalDate to,
        List<UtilizationReportRow> rows) {
}
