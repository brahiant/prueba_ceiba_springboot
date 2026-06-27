package com.deportal.users.dto;

import com.deportal.users.enums.CustomerType;
import com.deportal.users.enums.UserRole;

public record UserResponse(
        String userId,
        String name,
        String email,
        CustomerType customerType,
        UserRole role,
        boolean active) {
}
