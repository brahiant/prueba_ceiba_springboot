package com.deportal.courts.service;

import com.deportal.courts.dto.CourtResponse;
import com.deportal.courts.dto.CreateCourtRequest;
import com.deportal.courts.entity.CourtEntity;
import com.deportal.courts.mapper.CourtMapper;
import com.deportal.courts.repository.CourtRepository;
import com.deportal.shared.exception.BusinessException;
import com.deportal.shared.exception.ResourceNotFoundException;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourtService {

    private static final LocalTime GLOBAL_OPENING_TIME = LocalTime.of(6, 0);
    private static final LocalTime GLOBAL_CLOSING_TIME = LocalTime.of(22, 0);

    private final CourtRepository courtRepository;
    private final CourtMapper courtMapper;

    public CourtService(CourtRepository courtRepository, CourtMapper courtMapper) {
        this.courtRepository = courtRepository;
        this.courtMapper = courtMapper;
    }

    @Transactional(readOnly = true)
    public List<CourtResponse> findAll() {
        return courtRepository.findAll().stream()
                .map(courtMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CourtResponse findById(String courtId) {
        return courtRepository.findById(courtId)
                .map(courtMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("La cancha no existe"));
    }

    @Transactional
    public CourtResponse create(CreateCourtRequest request) {
        validateBusinessRules(request);

        CourtEntity court = courtMapper.toEntity(request);
        CourtEntity savedCourt = courtRepository.save(court);

        return courtMapper.toResponse(savedCourt);
    }

    private void validateBusinessRules(CreateCourtRequest request) {
        if (!request.openingTime().isBefore(request.closingTime())) {
            throw new BusinessException("La hora de apertura debe ser anterior a la hora de cierre");
        }

        if (request.openingTime().isBefore(GLOBAL_OPENING_TIME) || request.closingTime().isAfter(GLOBAL_CLOSING_TIME)) {
            throw new BusinessException("El horario debe estar dentro del rango global 06:00 a 22:00");
        }

        if (courtRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new BusinessException("Ya existe una cancha con ese nombre");
        }
    }
}
