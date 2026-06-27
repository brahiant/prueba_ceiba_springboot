package com.deportal.reservations.service;

import com.deportal.courts.entity.CourtEntity;
import com.deportal.courts.repository.CourtRepository;
import com.deportal.payments.model.PaymentCalculation;
import com.deportal.payments.service.PaymentCalculator;
import com.deportal.reservations.dto.CreateReservationRequest;
import com.deportal.reservations.dto.ReservationResponse;
import com.deportal.reservations.entity.ReservationEntity;
import com.deportal.reservations.enums.ReservationStatus;
import com.deportal.reservations.mapper.ReservationMapper;
import com.deportal.reservations.repository.ReservationRepository;
import com.deportal.shared.exception.BusinessException;
import com.deportal.shared.exception.ResourceNotFoundException;
import com.deportal.users.entity.UserEntity;
import com.deportal.users.repository.UserRepository;
import java.time.Clock;
import java.time.LocalDate;
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
    private final ReservationMapper reservationMapper;
    private final PaymentCalculator paymentCalculator;
    private final Clock clock;

    public ReservationService(
            ReservationRepository reservationRepository,
            CourtRepository courtRepository,
            UserRepository userRepository,
            ReservationMapper reservationMapper,
            PaymentCalculator paymentCalculator,
            Clock clock) {
        this.reservationRepository = reservationRepository;
        this.courtRepository = courtRepository;
        this.userRepository = userRepository;
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
        validateAvailability(court.getCourtId(), request.date(), request.startTime(), endTime);

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
                ReservationStatus.CONFIRMED);

        return reservationMapper.toResponse(reservationRepository.save(reservation));
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

    private void validateAvailability(String courtId, LocalDate date, LocalTime requestedStart, LocalTime requestedEnd) {
        List<ReservationEntity> confirmedReservations = reservationRepository.findByCourt_CourtIdAndDateAndStatus(
                courtId,
                date,
                ReservationStatus.CONFIRMED);

        boolean hasConflict = confirmedReservations.stream()
                .anyMatch(existing -> conflictsWithCleaningTime(requestedStart, requestedEnd, existing));

        if (hasConflict) {
            throw new BusinessException("La cancha no esta disponible en el horario solicitado");
        }
    }

    private boolean conflictsWithCleaningTime(LocalTime requestedStart, LocalTime requestedEnd, ReservationEntity existing) {
        LocalTime existingStartWithCleaning = existing.getStartTime().minusHours(CLEANING_HOURS);
        LocalTime existingEndWithCleaning = existing.getEndTime().plusHours(CLEANING_HOURS);

        return requestedStart.isBefore(existingEndWithCleaning) && requestedEnd.isAfter(existingStartWithCleaning);
    }
}
