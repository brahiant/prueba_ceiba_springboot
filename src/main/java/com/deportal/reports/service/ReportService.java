package com.deportal.reports.service;

import com.deportal.courts.entity.CourtEntity;
import com.deportal.courts.repository.CourtRepository;
import com.deportal.reports.dto.UtilizationReportResponse;
import com.deportal.reports.dto.UtilizationReportRow;
import com.deportal.reservations.entity.ReservationEntity;
import com.deportal.reservations.enums.ReservationStatus;
import com.deportal.reservations.repository.ReservationRepository;
import com.deportal.shared.exception.BusinessException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    private final CourtRepository courtRepository;
    private final ReservationRepository reservationRepository;

    public ReportService(CourtRepository courtRepository, ReservationRepository reservationRepository) {
        this.courtRepository = courtRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional(readOnly = true)
    public UtilizationReportResponse getUtilizationReport(LocalDate from, LocalDate to) {
        validateRange(from, to);

        List<CourtEntity> courts = courtRepository.findAll();
        List<ReservationEntity> reservations = reservationRepository.findByDateBetweenAndStatus(
                from,
                to,
                ReservationStatus.CONFIRMED);
        Map<String, List<ReservationEntity>> reservationsByCourt = reservations.stream()
                .collect(Collectors.groupingBy(reservation -> reservation.getCourt().getCourtId()));
        long days = ChronoUnit.DAYS.between(from, to) + 1;

        List<UtilizationReportRow> rows = courts.stream()
                .map(court -> buildRow(court, reservationsByCourt.getOrDefault(court.getCourtId(), List.of()), days))
                .toList();

        return new UtilizationReportResponse(from, to, rows);
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new BusinessException("El rango de fechas es obligatorio");
        }

        if (from.isAfter(to)) {
            throw new BusinessException("La fecha inicial no puede ser posterior a la fecha final");
        }
    }

    private UtilizationReportRow buildRow(CourtEntity court, List<ReservationEntity> reservations, long days) {
        int availableHours = Math.toIntExact(Duration.between(court.getOpeningTime(), court.getClosingTime()).toHours() * days);
        int reservedHours = reservations.stream()
                .mapToInt(ReservationEntity::getDurationHours)
                .sum();
        BigDecimal totalIncome = reservations.stream()
                .map(ReservationEntity::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal occupancyRate = calculateOccupancyRate(reservedHours, availableHours);

        return new UtilizationReportRow(
                court.getCourtId(),
                court.getName(),
                reservations.size(),
                reservedHours,
                availableHours,
                totalIncome,
                occupancyRate);
    }

    private BigDecimal calculateOccupancyRate(int reservedHours, int availableHours) {
        if (availableHours == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return BigDecimal.valueOf(reservedHours)
                .multiply(new BigDecimal("100"))
                .divide(BigDecimal.valueOf(availableHours), 2, RoundingMode.HALF_UP);
    }
}
