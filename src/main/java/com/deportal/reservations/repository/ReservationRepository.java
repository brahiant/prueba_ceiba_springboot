package com.deportal.reservations.repository;

import com.deportal.reservations.entity.ReservationEntity;
import com.deportal.reservations.enums.ReservationStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<ReservationEntity, String> {

    List<ReservationEntity> findByDateBetweenAndStatus(LocalDate from, LocalDate to, ReservationStatus status);
}
