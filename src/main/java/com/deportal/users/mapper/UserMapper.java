package com.deportal.users.mapper;

import com.deportal.auth.dto.RegisterRequest;
import com.deportal.shared.sanitization.StringSanitizer;
import com.deportal.users.dto.UserResponse;
import com.deportal.users.entity.UserEntity;
import com.deportal.users.enums.UserRole;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private final StringSanitizer sanitizer;

    public UserMapper(StringSanitizer sanitizer) {
        this.sanitizer = sanitizer;
    }

    public UserEntity toEntity(RegisterRequest request, String passwordHash) {
        return new UserEntity(
                sanitizer.clean(request.name()),
                sanitizer.clean(request.email()).toLowerCase(),
                passwordHash,
                request.customerType(),
                UserRole.USER,
                true);
    }

    public UserResponse toResponse(UserEntity entity) {
        return new UserResponse(
                entity.getUserId(),
                entity.getName(),
                entity.getEmail(),
                entity.getCustomerType(),
                entity.getRole(),
                entity.isActive());
    }
}
