package com.deportal.courts.controller;

import com.deportal.courts.dto.CourtResponse;
import com.deportal.courts.dto.CreateCourtRequest;
import com.deportal.courts.service.CourtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courts")
@Tag(name = "Courts", description = "Gestion de canchas deportivas")
public class CourtController {

    private final CourtService courtService;

    public CourtController(CourtService courtService) {
        this.courtService = courtService;
    }

    @GetMapping
    @Operation(summary = "Lista las canchas registradas")
    @ApiResponse(responseCode = "200", description = "Listado de canchas")
    public List<CourtResponse> findAll() {
        return courtService.findAll();
    }

    @GetMapping("/{courtId}")
    @Operation(summary = "Consulta una cancha por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cancha encontrada"),
            @ApiResponse(responseCode = "404", description = "Cancha no encontrada")
    })
    public CourtResponse findById(@PathVariable String courtId) {
        return courtService.findById(courtId);
    }

    @PostMapping
    @Operation(summary = "Registra una nueva cancha")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cancha creada"),
            @ApiResponse(responseCode = "400", description = "Request invalido"),
            @ApiResponse(responseCode = "409", description = "Regla de negocio incumplida")
    })
    public ResponseEntity<CourtResponse> create(@Valid @RequestBody CreateCourtRequest request) {
        CourtResponse response = courtService.create(request);

        return ResponseEntity
                .created(URI.create("/api/courts/" + response.courtId()))
                .body(response);
    }
}
