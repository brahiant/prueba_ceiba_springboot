package com.deportal.reservations.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deportal.courts.entity.CourtEntity;
import com.deportal.courts.enums.SportType;
import com.deportal.courts.repository.CourtRepository;
import com.deportal.payments.service.PaymentCalculator;
import com.deportal.reservations.dto.CreateReservationRequest;
import com.deportal.reservations.dto.ReservationResponse;
import com.deportal.reservations.entity.ReservationEntity;
import com.deportal.reservations.enums.ReservationStatus;
import com.deportal.reservations.mapper.ReservationMapper;
import com.deportal.reservations.repository.ReservationRepository;
import com.deportal.shared.exception.BusinessException;
import com.deportal.users.entity.UserEntity;
import com.deportal.users.enums.CustomerType;
import com.deportal.users.enums.UserRole;
import com.deportal.users.repository.UserRepository;
import com.deportal.waitlist.entity.WaitlistEntryEntity;
import com.deportal.waitlist.enums.WaitlistStatus;
import com.deportal.waitlist.repository.WaitlistEntryRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-06-20T12:00:00Z"), ZoneId.of("UTC"));
    private static final String RESERVATION_ID = "reservation-id";

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private CourtRepository courtRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WaitlistEntryRepository waitlistEntryRepository;

    private ReservationService reservationService;

    private UserEntity user;
    private CourtEntity court;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(
                reservationRepository,
                courtRepository,
                userRepository,
                waitlistEntryRepository,
                new ReservationMapper(),
                new PaymentCalculator(),
                FIXED_CLOCK);
        user = new UserEntity("Juan Perez", "juan@deportal.local", "hash", CustomerType.MIEMBRO, UserRole.USER, true);
        court = new CourtEntity(
                "Cancha Central",
                SportType.FUTBOL,
                22,
                LocalTime.of(6, 0),
                LocalTime.of(22, 0),
                new BigDecimal("20.00"),
                true);
    }

    @Test
    void shouldCreateReservationWhenThereIsAvailability() {
        CreateReservationRequest request = validRequest(LocalTime.of(10, 0), 2);
        mockUserAndCourt(request);
        when(reservationRepository.findByCourt_CourtIdAndDateAndStatus(court.getCourtId(), request.date(), ReservationStatus.CONFIRMED))
                .thenReturn(List.of());
        when(reservationRepository.save(any(ReservationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReservationResponse response = reservationService.create(request);

        assertThat(response.customerName()).isEqualTo("Juan Perez");
        assertThat(response.startTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(response.endTime()).isEqualTo(LocalTime.of(12, 0));
        assertThat(response.durationHours()).isEqualTo(2);
        assertThat(response.baseAmount()).isEqualByComparingTo("40.00");
        assertThat(response.memberDiscount()).isEqualByComparingTo("4.00");
        assertThat(response.totalDiscount()).isEqualByComparingTo("4.00");
        assertThat(response.totalAmount()).isEqualByComparingTo("36.00");
        assertThat(response.status()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void shouldRejectPastDate() {
        CreateReservationRequest request = new CreateReservationRequest(
                "user-id",
                "court-id",
                LocalDate.of(2026, 6, 19),
                LocalTime.of(10, 0),
                2,
                CustomerType.MIEMBRO);
        mockUserAndCourt(request);

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("La fecha de reservacion no puede estar en el pasado");

        verify(reservationRepository, never()).save(any(ReservationEntity.class));
    }

    @Test
    void shouldRejectReservationOutsideCourtSchedule() {
        court = new CourtEntity(
                "Cancha Norte",
                SportType.BASQUET,
                10,
                LocalTime.of(7, 0),
                LocalTime.of(21, 0),
                new BigDecimal("15.00"),
                true);
        CreateReservationRequest request = validRequest(LocalTime.of(6, 0), 1);
        mockUserAndCourt(request);

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("La reservacion debe estar dentro del horario de operacion de la cancha");

        verify(reservationRepository, never()).save(any(ReservationEntity.class));
    }

    @Test
    void shouldRejectOverlappingReservation() {
        CreateReservationRequest request = validRequest(LocalTime.of(11, 0), 2);
        ReservationEntity existing = existingReservation(LocalTime.of(10, 0), 2);
        mockUserAndCourt(request);
        when(reservationRepository.findByCourt_CourtIdAndDateAndStatus(court.getCourtId(), request.date(), ReservationStatus.CONFIRMED))
                .thenReturn(List.of(existing));

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("La cancha no esta disponible en el horario solicitado");
    }

    @Test
    void shouldRejectReservationWithoutCleaningTime() {
        CreateReservationRequest request = validRequest(LocalTime.of(12, 0), 1);
        ReservationEntity existing = existingReservation(LocalTime.of(10, 0), 2);
        mockUserAndCourt(request);
        when(reservationRepository.findByCourt_CourtIdAndDateAndStatus(court.getCourtId(), request.date(), ReservationStatus.CONFIRMED))
                .thenReturn(List.of(existing));

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("La cancha no esta disponible en el horario solicitado");
    }

    @Test
    void shouldAllowReservationWithOneHourCleaningTime() {
        CreateReservationRequest request = validRequest(LocalTime.of(13, 0), 1);
        ReservationEntity existing = existingReservation(LocalTime.of(10, 0), 2);
        mockUserAndCourt(request);
        when(reservationRepository.findByCourt_CourtIdAndDateAndStatus(court.getCourtId(), request.date(), ReservationStatus.CONFIRMED))
                .thenReturn(List.of(existing));
        when(reservationRepository.save(any(ReservationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReservationResponse response = reservationService.create(request);

        assertThat(response.startTime()).isEqualTo(LocalTime.of(13, 0));
        assertThat(response.endTime()).isEqualTo(LocalTime.of(14, 0));
    }

    @Test
    void shouldCreateWaitlistForNonMemberSameDayWhenThereIsNoAvailability() {
        CreateReservationRequest request = new CreateReservationRequest(
                "user-id",
                "court-id",
                LocalDate.of(2026, 6, 20),
                LocalTime.of(14, 0),
                1,
                CustomerType.NO_MIEMBRO);
        ReservationEntity existing = existingReservation(LocalDate.of(2026, 6, 20), LocalTime.of(13, 0), 2);
        mockUserAndCourt(request);
        when(reservationRepository.findByCourt_CourtIdAndDateAndStatus(court.getCourtId(), request.date(), ReservationStatus.CONFIRMED))
                .thenReturn(List.of(existing));
        when(reservationRepository.save(any(ReservationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(waitlistEntryRepository.save(any(WaitlistEntryEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReservationResponse response = reservationService.create(request);

        assertThat(response.status()).isEqualTo(ReservationStatus.WAITLISTED);
        verify(waitlistEntryRepository).save(any(WaitlistEntryEntity.class));
    }

    @Test
    void shouldRefundFullAmountWhenCancellationIsMoreThanTwentyFourHoursBeforeStart() {
        ReservationEntity reservation = existingReservation(LocalDate.of(2026, 6, 22), LocalTime.of(13, 0), 2);
        mockCancellableReservationWithoutWaitlist(reservation);

        var response = reservationService.cancel(RESERVATION_ID);

        assertThat(response.status()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(response.refundAmount()).isEqualByComparingTo("40.00");
    }

    @Test
    void shouldRefundHalfAmountWhenCancellationIsBetweenTwoAndTwentyFourHoursBeforeStart() {
        ReservationEntity reservation = existingReservation(LocalDate.of(2026, 6, 21), LocalTime.of(10, 0), 2);
        mockCancellableReservationWithoutWaitlist(reservation);

        var response = reservationService.cancel(RESERVATION_ID);

        assertThat(response.status()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(response.refundAmount()).isEqualByComparingTo("20.00");
    }

    @Test
    void shouldNotRefundWhenCancellationIsLessThanTwoHoursBeforeStart() {
        ReservationEntity reservation = existingReservation(LocalDate.of(2026, 6, 20), LocalTime.of(13, 0), 1);
        mockCancellableReservationWithoutWaitlist(reservation);

        var response = reservationService.cancel(RESERVATION_ID);

        assertThat(response.status()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(response.refundAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    void shouldRejectCancellationWhenReservationAlreadyOccurred() {
        ReservationEntity reservation = existingReservation(LocalDate.of(2026, 6, 20), LocalTime.of(11, 0), 1);
        when(reservationRepository.findById(RESERVATION_ID)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancel(RESERVATION_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Solo se puede cancelar una reservacion futura");
    }

    @Test
    void shouldRejectCancellationWhenReservationIsAlreadyCancelled() {
        ReservationEntity reservation = existingReservation(LocalDate.of(2026, 6, 22), LocalTime.of(13, 0), 2);
        reservation.cancel(new BigDecimal("40.00"), Instant.now(FIXED_CLOCK));
        when(reservationRepository.findById(RESERVATION_ID)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancel(RESERVATION_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessage("La reservacion ya fue cancelada");

        verify(reservationRepository, never()).save(any(ReservationEntity.class));
    }

    @Test
    void shouldActivateFirstCompatibleWaitlistWhenCancellingReservation() {
        ReservationEntity confirmedReservation = existingReservation(LocalDate.of(2026, 6, 22), LocalTime.of(13, 0), 2);
        ReservationEntity waitlistedReservation = new ReservationEntity(
                user,
                court,
                user.getName(),
                CustomerType.NO_MIEMBRO,
                confirmedReservation.getDate(),
                confirmedReservation.getStartTime(),
                confirmedReservation.getEndTime(),
                confirmedReservation.getDurationHours(),
                new BigDecimal("40.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("40.00"),
                ReservationStatus.WAITLISTED);
        WaitlistEntryEntity waitlistEntry = new WaitlistEntryEntity(
                waitlistedReservation,
                user,
                court,
                confirmedReservation.getDate(),
                confirmedReservation.getStartTime(),
                confirmedReservation.getEndTime(),
                confirmedReservation.getDurationHours(),
                WaitlistStatus.WAITING);
        when(reservationRepository.findById(RESERVATION_ID)).thenReturn(Optional.of(confirmedReservation));
        when(reservationRepository.save(any(ReservationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(waitlistEntryRepository.findByCourt_CourtIdAndDateAndStartTimeAndEndTimeAndStatusOrderByCreatedAtAsc(
                court.getCourtId(), confirmedReservation.getDate(), confirmedReservation.getStartTime(), confirmedReservation.getEndTime(), WaitlistStatus.WAITING))
                .thenReturn(List.of(waitlistEntry));

        reservationService.cancel(RESERVATION_ID);

        assertThat(waitlistedReservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(waitlistEntry.getStatus()).isEqualTo(WaitlistStatus.ACTIVATED);
    }

    private void mockUserAndCourt(CreateReservationRequest request) {
        when(userRepository.findById(request.userId())).thenReturn(Optional.of(user));
        when(courtRepository.findById(request.courtId())).thenReturn(Optional.of(court));
    }

    private void mockCancellableReservationWithoutWaitlist(ReservationEntity reservation) {
        when(reservationRepository.findById(RESERVATION_ID)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(ReservationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(waitlistEntryRepository.findByCourt_CourtIdAndDateAndStartTimeAndEndTimeAndStatusOrderByCreatedAtAsc(
                court.getCourtId(), reservation.getDate(), reservation.getStartTime(), reservation.getEndTime(), WaitlistStatus.WAITING))
                .thenReturn(List.of());
    }

    private CreateReservationRequest validRequest(LocalTime startTime, int durationHours) {
        return new CreateReservationRequest(
                "user-id",
                "court-id",
                LocalDate.of(2026, 6, 21),
                startTime,
                durationHours,
                CustomerType.MIEMBRO);
    }

    private ReservationEntity existingReservation(LocalTime startTime, int durationHours) {
        return existingReservation(LocalDate.of(2026, 6, 21), startTime, durationHours);
    }

    private ReservationEntity existingReservation(LocalDate date, LocalTime startTime, int durationHours) {
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
                new BigDecimal("40.00"),
                ReservationStatus.CONFIRMED);
    }
}
