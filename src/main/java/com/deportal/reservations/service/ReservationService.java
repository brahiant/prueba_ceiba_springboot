package com.deportal.reservations.service;

import com.deportal.courts.entity.CourtEntity;
import com.deportal.courts.repository.CourtRepository;
import com.deportal.payments.model.PaymentCalculation;
import com.deportal.payments.service.PaymentCalculator;
import com.deportal.reservations.dto.CancelReservationResponse;
import com.deportal.reservations.dto.CreateReservationRequest;
import com.deportal.reservations.dto.ReservationResponse;
import com.deportal.reservations.entity.ReservationEntity;
import com.deportal.reservations.enums.ReservationStatus;
import com.deportal.reservations.mapper.ReservationMapper;
import com.deportal.reservations.repository.ReservationRepository;
import com.deportal.shared.exception.BusinessException;
import com.deportal.shared.exception.ResourceNotFoundException;
import com.deportal.users.entity.UserEntity;
import com.deportal.users.enums.CustomerType;
import com.deportal.users.repository.UserRepository;
import com.deportal.waitlist.entity.WaitlistEntryEntity;
import com.deportal.waitlist.enums.WaitlistStatus;
import com.deportal.waitlist.repository.WaitlistEntryRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {

    private static final LocalTime GLOBAL_OPENING_TIME = LocalTime.of(6, 0);
    private static final LocalTime GLOBAL_CLOSING_TIME = LocalTime.of(22, 0);
    private static final int CLEANING_HOURS = 1;

    private final ReservationRepository reservationRepository;
    private final CourtRepository courtRepository;
    private final UserRepository userRepository;
    private final WaitlistEntryRepository waitlistEntryRepository;
    private final ReservationMapper reservationMapper;
    private final PaymentCalculator paymentCalculator;
    private final Clock clock;

    public ReservationService(
            ReservationRepository reservationRepository,
            CourtRepository courtRepository,
            UserRepository userRepository,
            WaitlistEntryRepository waitlistEntryRepository,
            ReservationMapper reservationMapper,
            PaymentCalculator paymentCalculator,
            Clock clock) {
        this.reservationRepository = reservationRepository;
        this.courtRepository = courtRepository;
        this.userRepository = userRepository;
        this.waitlistEntryRepository = waitlistEntryRepository;
        this.reservationMapper = reservationMapper;
        this.paymentCalculator = paymentCalculator;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll().stream()
                .map(reservationMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReservationResponse findById(String reservationId) {
        return reservationRepository.findById(reservationId)
                .map(reservationMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("La reservacion no existe"));
    }

    @Transactional
    public ReservationResponse create(CreateReservationRequest request) {
        UserEntity user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("El usuario no existe"));
        CourtEntity court = courtRepository.findById(request.courtId())
                .orElseThrow(() -> new ResourceNotFoundException("La cancha no existe"));

        LocalTime endTime = request.startTime().plusHours(request.durationHours());

        validateReservationWindow(request, court, endTime);
        boolean available = isAvailable(court.getCourtId(), request.date(), request.startTime(), endTime);

        if (!available && shouldCreateWaitlist(request)) {
            return createReservation(request, user, court, endTime, ReservationStatus.WAITLISTED);
        }

        if (!available) {
            throw new BusinessException("La cancha no esta disponible en el horario solicitado");
        }

        return createReservation(request, user, court, endTime, ReservationStatus.CONFIRMED);
    }

    @Transactional
    public CancelReservationResponse cancel(String reservationId) {
        ReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("La reservacion no existe"));

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BusinessException("La reservacion ya fue cancelada");
        }

        LocalDateTime reservationStart = LocalDateTime.of(reservation.getDate(), reservation.getStartTime());
        LocalDateTime now = LocalDateTime.now(clock);

        if (!reservationStart.isAfter(now)) {
            throw new BusinessException("Solo se puede cancelar una reservacion futura");
        }

        BigDecimal refundAmount = calculateRefund(reservation, now, reservationStart);
        reservation.cancel(refundAmount, Instant.now(clock));
        ReservationEntity cancelledReservation = reservationRepository.save(reservation);

        activateFirstCompatibleWaitlist(cancelledReservation);

        return reservationMapper.toCancelResponse(cancelledReservation);
    }

    private ReservationResponse createReservation(
            CreateReservationRequest request,
            UserEntity user,
            CourtEntity court,
            LocalTime endTime,
            ReservationStatus status) {
        PaymentCalculation payment = paymentCalculator.calculate(
                court.getHourlyRate(),
                request.durationHours(),
                request.customerType(),
                request.startTime());
        ReservationEntity reservation = new ReservationEntity(
                user,
                court,
                user.getName(),
                request.customerType(),
                request.date(),
                request.startTime(),
                endTime,
                request.durationHours(),
                payment.baseAmount(),
                payment.memberDiscount(),
                payment.offPeakDiscount(),
                payment.totalDiscount(),
                payment.totalAmount(),
                status);
        ReservationEntity savedReservation = reservationRepository.save(reservation);

        if (status == ReservationStatus.WAITLISTED) {
            waitlistEntryRepository.save(new WaitlistEntryEntity(
                    savedReservation,
                    user,
                    court,
                    request.date(),
                    request.startTime(),
                    endTime,
                    request.durationHours(),
                    WaitlistStatus.WAITING));
        }

        return reservationMapper.toResponse(savedReservation);
    }

    private void validateReservationWindow(CreateReservationRequest request, CourtEntity court, LocalTime endTime) {
        LocalDate today = LocalDate.now(clock);

        if (request.date().isBefore(today)) {
            throw new BusinessException("La fecha de reservacion no puede estar en el pasado");
        }

        if (request.startTime().isBefore(GLOBAL_OPENING_TIME) || endTime.isAfter(GLOBAL_CLOSING_TIME)) {
            throw new BusinessException("La reservacion debe estar dentro del horario global 06:00 a 22:00");
        }

        if (request.startTime().isBefore(court.getOpeningTime()) || endTime.isAfter(court.getClosingTime())) {
            throw new BusinessException("La reservacion debe estar dentro del horario de operacion de la cancha");
        }

        if (!request.startTime().isBefore(endTime)) {
            throw new BusinessException("La hora de inicio debe ser anterior a la hora de fin");
        }
    }

    private boolean isAvailable(String courtId, LocalDate date, LocalTime requestedStart, LocalTime requestedEnd) {
        List<ReservationEntity> confirmedReservations = reservationRepository.findByCourt_CourtIdAndDateAndStatus(
                courtId,
                date,
                ReservationStatus.CONFIRMED);

        return confirmedReservations.stream()
                .noneMatch(existing -> conflictsWithCleaningTime(requestedStart, requestedEnd, existing));
    }

    private boolean conflictsWithCleaningTime(LocalTime requestedStart, LocalTime requestedEnd, ReservationEntity existing) {
        LocalTime existingStartWithCleaning = existing.getStartTime().minusHours(CLEANING_HOURS);
        LocalTime existingEndWithCleaning = existing.getEndTime().plusHours(CLEANING_HOURS);

        return requestedStart.isBefore(existingEndWithCleaning) && requestedEnd.isAfter(existingStartWithCleaning);
    }

    private boolean shouldCreateWaitlist(CreateReservationRequest request) {
        return request.date().isEqual(LocalDate.now(clock)) && request.customerType() == CustomerType.NO_MIEMBRO;
    }

    private BigDecimal calculateRefund(ReservationEntity reservation, LocalDateTime now, LocalDateTime reservationStart) {
        long minutesUntilReservation = Duration.between(now, reservationStart).toMinutes();

        if (minutesUntilReservation > 24 * 60) {
            return reservation.getTotalAmount();
        }

        if (minutesUntilReservation >= 2 * 60) {
            return reservation.getTotalAmount().multiply(new BigDecimal("0.50")).setScale(2, RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private void activateFirstCompatibleWaitlist(ReservationEntity cancelledReservation) {
        List<WaitlistEntryEntity> waitlistEntries = waitlistEntryRepository
                .findByCourt_CourtIdAndDateAndStartTimeAndEndTimeAndStatusOrderByCreatedAtAsc(
                        cancelledReservation.getCourt().getCourtId(),
                        cancelledReservation.getDate(),
                        cancelledReservation.getStartTime(),
                        cancelledReservation.getEndTime(),
                        WaitlistStatus.WAITING);

        if (waitlistEntries.isEmpty()) {
            return;
        }

        WaitlistEntryEntity waitlistEntry = waitlistEntries.getFirst();
        waitlistEntry.getReservation().confirm();
        waitlistEntry.activate();
        reservationRepository.save(waitlistEntry.getReservation());
        waitlistEntryRepository.save(waitlistEntry);
    }
}
