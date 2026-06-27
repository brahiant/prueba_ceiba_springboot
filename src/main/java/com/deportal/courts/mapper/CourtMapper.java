package com.deportal.courts.mapper;

import com.deportal.courts.dto.CourtResponse;
import com.deportal.courts.dto.CreateCourtRequest;
import com.deportal.courts.entity.CourtEntity;
import com.deportal.shared.sanitization.StringSanitizer;
import org.springframework.stereotype.Component;

@Component
public class CourtMapper {

    private final StringSanitizer sanitizer;

    public CourtMapper(StringSanitizer sanitizer) {
        this.sanitizer = sanitizer;
    }

    public CourtEntity toEntity(CreateCourtRequest request) {
        return new CourtEntity(
                sanitizer.clean(request.name()),
                request.sportType(),
                request.capacity(),
                request.openingTime(),
                request.closingTime(),
                request.hourlyRate(),
                true);
    }

    public CourtResponse toResponse(CourtEntity entity) {
        return new CourtResponse(
                entity.getCourtId(),
                entity.getName(),
                entity.getSportType(),
                entity.getCapacity(),
                entity.getOpeningTime(),
                entity.getClosingTime(),
                entity.getHourlyRate(),
                entity.isActive());
    }
}
