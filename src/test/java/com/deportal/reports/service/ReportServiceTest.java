package com.deportal.reports.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deportal.courts.entity.CourtEntity;
import com.deportal.courts.enums.SportType;
import com.deportal.courts.repository.CourtRepository;
import com.deportal.reservations.entity.ReservationEntity;
import com.deportal.reservations.enums.ReservationStatus;
import com.deportal.reservations.repository.ReservationRepository;
import com.deportal.shared.exception.BusinessException;
import com.deportal.users.entity.UserEntity;
import com.deportal.users.enums.CustomerType;
import com.deportal.users.enums.UserRole;
import java.math.BigDecimal;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private CourtRepository courtRepository;

    @Mock
    private ReservationRepository reservationRepository;

    private ReportService reportService;
    private CourtEntity court;
    private UserEntity user;

    @BeforeEach
    void setUp() throws Exception {
        reportService = new ReportService(courtRepository, reservationRepository);
        court = new CourtEntity(
                "Cancha Central",
                SportType.FUTBOL,
                22,
                LocalTime.of(6, 0),
                LocalTime.of(22, 0),
                new BigDecimal("20.00"),
                true);
        setCourtId(court, "court-1");
        user = new UserEntity("Juan Perez", "juan@deportal.local", "hash", CustomerType.MIEMBRO, UserRole.USER, true);
    }

    @Test
    void shouldGenerateUtilizationReportForRange() {
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 2);
        ReservationEntity reservationOne = reservation(LocalDate.of(2026, 6, 1), LocalTime.of(10, 0), 2, "36.00");
        ReservationEntity reservationTwo = reservation(LocalDate.of(2026, 6, 2), LocalTime.of(13, 0), 1, "18.00");
        when(courtRepository.findAll()).thenReturn(List.of(court));
        when(reservationRepository.findByDateBetweenAndStatus(from, to, ReservationStatus.CONFIRMED))
                .thenReturn(List.of(reservationOne, reservationTwo));

        var response = reportService.getUtilizationReport(from, to);

        assertThat(response.from()).isEqualTo(from);
        assertThat(response.to()).isEqualTo(to);
        assertThat(response.rows()).hasSize(1);
        var row = response.rows().getFirst();
        assertThat(row.courtName()).isEqualTo("Cancha Central");
        assertThat(row.totalReservations()).isEqualTo(2);
        assertThat(row.reservedHours()).isEqualTo(3);
        assertThat(row.availableHours()).isEqualTo(32);
        assertThat(row.totalIncome()).isEqualByComparingTo("54.00");
        assertThat(row.occupancyRate()).isEqualByComparingTo("9.38");
        verify(reservationRepository).findByDateBetweenAndStatus(from, to, ReservationStatus.CONFIRMED);
    }

    @Test
    void shouldReturnZeroValuesWhenCourtHasNoReservations() {
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 1);
        when(courtRepository.findAll()).thenReturn(List.of(court));
        when(reservationRepository.findByDateBetweenAndStatus(from, to, ReservationStatus.CONFIRMED))
                .thenReturn(List.of());

        var row = reportService.getUtilizationReport(from, to).rows().getFirst();

        assertThat(row.totalReservations()).isZero();
        assertThat(row.reservedHours()).isZero();
        assertThat(row.availableHours()).isEqualTo(16);
        assertThat(row.totalIncome()).isEqualByComparingTo("0.00");
        assertThat(row.occupancyRate()).isEqualByComparingTo("0.00");
    }

    @Test
    void shouldRejectInvalidDateRange() {
        assertThatThrownBy(() -> reportService.getUtilizationReport(LocalDate.of(2026, 6, 2), LocalDate.of(2026, 6, 1)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("La fecha inicial no puede ser posterior a la fecha final");
    }

    private ReservationEntity reservation(LocalDate date, LocalTime startTime, int durationHours, String totalAmount) {
        return new ReservationEntity(
                user,
                court,
                user.getName(),
                CustomerType.MIEMBRO,
                date,
                startTime,
                startTime.plusHours(durationHours),
                durationHours,
                new BigDecimal("40.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal(totalAmount),
                ReservationStatus.CONFIRMED);
    }

    private void setCourtId(CourtEntity court, String courtId) throws Exception {
        Field field = CourtEntity.class.getDeclaredField("courtId");
        field.setAccessible(true);
        field.set(court, courtId);
    }
}
