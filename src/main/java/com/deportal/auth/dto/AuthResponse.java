package com.deportal.auth.dto;

import com.deportal.users.dto.UserResponse;

public record AuthResponse(String token, long expiresInSeconds, UserResponse user) {
}
