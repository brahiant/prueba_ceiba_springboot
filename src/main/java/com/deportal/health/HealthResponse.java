package com.deportal.health;

import java.time.Instant;

public record HealthResponse(String status, Instant timestamp) {
}
