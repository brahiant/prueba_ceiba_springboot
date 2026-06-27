package com.deportal.courts.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deportal.courts.dto.CourtResponse;
import com.deportal.courts.dto.CreateCourtRequest;
import com.deportal.courts.entity.CourtEntity;
import com.deportal.courts.enums.SportType;
import com.deportal.courts.mapper.CourtMapper;
import com.deportal.courts.repository.CourtRepository;
import com.deportal.shared.exception.BusinessException;
import com.deportal.shared.exception.ResourceNotFoundException;
import com.deportal.shared.sanitization.StringSanitizer;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourtServiceTest {

    @Mock
    private CourtRepository courtRepository;

    private CourtService courtService;

    @BeforeEach
    void setUp() {
        CourtMapper courtMapper = new CourtMapper(new StringSanitizer());
        courtService = new CourtService(courtRepository, courtMapper);
    }

    @Test
    void shouldCreateCourtWhenRequestIsValid() {
        CreateCourtRequest request = validRequest();
        when(courtRepository.existsByNameIgnoreCase("Cancha Nueva")).thenReturn(false);
        when(courtRepository.save(any(CourtEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CourtResponse response = courtService.create(request);

        assertThat(response.name()).isEqualTo("Cancha Nueva");
        assertThat(response.sportType()).isEqualTo(SportType.FUTBOL);
        assertThat(response.capacity()).isEqualTo(22);
        assertThat(response.active()).isTrue();
        verify(courtRepository).save(any(CourtEntity.class));
    }

    @Test
    void shouldRejectDuplicatedName() {
        CreateCourtRequest request = validRequest();
        when(courtRepository.existsByNameIgnoreCase("Cancha Nueva")).thenReturn(true);

        assertThatThrownBy(() -> courtService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Ya existe una cancha con ese nombre");

        verify(courtRepository, never()).save(any(CourtEntity.class));
    }

    @Test
    void shouldRejectOpeningTimeAfterClosingTime() {
        CreateCourtRequest request = new CreateCourtRequest(
                "Cancha Nueva",
                SportType.FUTBOL,
                22,
                LocalTime.of(18, 0),
                LocalTime.of(8, 0),
                new BigDecimal("20.00"));

        assertThatThrownBy(() -> courtService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("La hora de apertura debe ser anterior a la hora de cierre");

        verify(courtRepository, never()).save(any(CourtEntity.class));
    }

    @Test
    void shouldRejectScheduleOutsideGlobalRange() {
        CreateCourtRequest request = new CreateCourtRequest(
                "Cancha Nueva",
                SportType.FUTBOL,
                22,
                LocalTime.of(5, 0),
                LocalTime.of(22, 0),
                new BigDecimal("20.00"));

        assertThatThrownBy(() -> courtService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("El horario debe estar dentro del rango global 06:00 a 22:00");

        verify(courtRepository, never()).save(any(CourtEntity.class));
    }

    @Test
    void shouldThrowNotFoundWhenCourtDoesNotExist() {
        when(courtRepository.findById("missing-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courtService.findById("missing-id"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("La cancha no existe");
    }

    private CreateCourtRequest validRequest() {
        return new CreateCourtRequest(
                "Cancha Nueva",
                SportType.FUTBOL,
                22,
                LocalTime.of(6, 0),
                LocalTime.of(22, 0),
                new BigDecimal("20.00"));
    }
}
