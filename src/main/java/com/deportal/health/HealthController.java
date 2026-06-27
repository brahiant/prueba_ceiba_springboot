package com.deportal.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "Estado basico de la API")
public class HealthController {

    @GetMapping
    @Operation(summary = "Verifica que la API este disponible")
    public HealthResponse health() {
        return new HealthResponse("UP", Instant.now());
    }
}
