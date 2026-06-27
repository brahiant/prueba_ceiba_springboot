package com.deportal.reservations.controller;

import com.deportal.reservations.dto.CreateReservationRequest;
import com.deportal.reservations.dto.CancelReservationResponse;
import com.deportal.reservations.dto.ReservationResponse;
import com.deportal.reservations.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@RequestMapping("/api/reservations")
@Tag(name = "Reservations", description = "Gestion de reservaciones de canchas")
@SecurityRequirement(name = "bearerAuth")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    @Operation(summary = "Lista las reservaciones")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de reservaciones"),
            @ApiResponse(responseCode = "401", description = "Token ausente o invalido")
    })
    public List<ReservationResponse> findAll() {
        return reservationService.findAll();
    }

    @GetMapping("/{reservationId}")
    @Operation(summary = "Consulta una reservacion por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservacion encontrada"),
            @ApiResponse(responseCode = "401", description = "Token ausente o invalido"),
            @ApiResponse(responseCode = "404", description = "Reservacion no encontrada")
    })
    public ReservationResponse findById(@PathVariable String reservationId) {
        return reservationService.findById(reservationId);
    }

    @PostMapping
    @Operation(summary = "Crea una reservacion confirmada si hay disponibilidad")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reservacion creada"),
            @ApiResponse(responseCode = "400", description = "Request invalido"),
            @ApiResponse(responseCode = "401", description = "Token ausente o invalido"),
            @ApiResponse(responseCode = "404", description = "Usuario o cancha no encontrado"),
            @ApiResponse(responseCode = "409", description = "Regla de negocio incumplida")
    })
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody CreateReservationRequest request) {
        ReservationResponse response = reservationService.create(request);

        return ResponseEntity
                .created(URI.create("/api/reservations/" + response.reservationId()))
                .body(response);
    }

    @PostMapping("/{reservationId}/cancel")
    @Operation(summary = "Cancela una reservacion futura y calcula el reembolso")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservacion cancelada"),
            @ApiResponse(responseCode = "401", description = "Token ausente o invalido"),
            @ApiResponse(responseCode = "404", description = "Reservacion no encontrada"),
            @ApiResponse(responseCode = "409", description = "La reservacion no se puede cancelar")
    })
    public CancelReservationResponse cancel(@PathVariable String reservationId) {
        return reservationService.cancel(reservationId);
    }
}
