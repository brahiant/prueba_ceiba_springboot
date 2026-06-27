package com.deportal.waitlist.repository;

import com.deportal.waitlist.entity.WaitlistEntryEntity;
import com.deportal.waitlist.enums.WaitlistStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WaitlistEntryRepository extends JpaRepository<WaitlistEntryEntity, String> {

    List<WaitlistEntryEntity> findByStatusOrderByCreatedAtAsc(WaitlistStatus status);

    List<WaitlistEntryEntity> findByCourt_CourtIdAndDateAndStartTimeAndEndTimeAndStatusOrderByCreatedAtAsc(
            String courtId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            WaitlistStatus status);
}
