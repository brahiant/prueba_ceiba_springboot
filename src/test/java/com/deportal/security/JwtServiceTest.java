package com.deportal.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.deportal.users.entity.UserEntity;
import com.deportal.users.enums.CustomerType;
import com.deportal.users.enums.UserRole;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "deportal-test-secret-key-must-have-at-least-32-bytes";

    @Test
    void shouldGenerateAndValidateToken() throws Exception {
        JwtService jwtService = new JwtService(SECRET, 28800000L);
        UserEntity user = new UserEntity("Ana Perez", "ana@mail.com", "hash", CustomerType.MIEMBRO, UserRole.USER, true);
        setUserId(user, "user-123");

        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractUserId(token)).isEqualTo("user-123");
        assertThat(jwtService.getExpirationSeconds()).isEqualTo(28800L);
    }

    @Test
    void shouldRejectInvalidToken() {
        JwtService jwtService = new JwtService(SECRET, 28800000L);

        assertThat(jwtService.isTokenValid("invalid-token")).isFalse();
    }

    private void setUserId(UserEntity user, String userId) throws Exception {
        Field field = UserEntity.class.getDeclaredField("userId");
        field.setAccessible(true);
        field.set(user, userId);
    }
}
