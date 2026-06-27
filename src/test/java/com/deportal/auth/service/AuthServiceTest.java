package com.deportal.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deportal.auth.dto.LoginRequest;
import com.deportal.auth.dto.RegisterRequest;
import com.deportal.security.JwtService;
import com.deportal.shared.exception.BusinessException;
import com.deportal.shared.sanitization.StringSanitizer;
import com.deportal.users.entity.UserEntity;
import com.deportal.users.enums.CustomerType;
import com.deportal.users.enums.UserRole;
import com.deportal.users.mapper.UserMapper;
import com.deportal.users.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    private AuthService authService;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(
                userRepository,
                new UserMapper(new StringSanitizer()),
                passwordEncoder,
                jwtService);
    }

    @Test
    void shouldRegisterUserAndReturnToken() {
        RegisterRequest request = new RegisterRequest(" Ana  Perez ", "ANA@MAIL.COM", "Password123", CustomerType.MIEMBRO);
        when(userRepository.existsByEmailIgnoreCase("ana@mail.com")).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(UserEntity.class))).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(28800L);

        var response = authService.register(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.expiresInSeconds()).isEqualTo(28800L);
        assertThat(response.user().name()).isEqualTo("Ana Perez");
        assertThat(response.user().email()).isEqualTo("ana@mail.com");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void shouldRejectDuplicatedEmailOnRegister() {
        RegisterRequest request = new RegisterRequest("Ana Perez", "ana@mail.com", "Password123", CustomerType.MIEMBRO);
        when(userRepository.existsByEmailIgnoreCase("ana@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Ya existe un usuario con ese email");
    }

    @Test
    void shouldLoginWithValidCredentials() {
        UserEntity user = new UserEntity(
                "Ana Perez",
                "ana@mail.com",
                passwordEncoder.encode("Password123"),
                CustomerType.MIEMBRO,
                UserRole.USER,
                true);
        when(userRepository.findByEmailIgnoreCase("ana@mail.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(28800L);

        var response = authService.login(new LoginRequest("ana@mail.com", "Password123"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("ana@mail.com");
    }

    @Test
    void shouldRejectInvalidPassword() {
        UserEntity user = new UserEntity(
                "Ana Perez",
                "ana@mail.com",
                passwordEncoder.encode("Password123"),
                CustomerType.MIEMBRO,
                UserRole.USER,
                true);
        when(userRepository.findByEmailIgnoreCase("ana@mail.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("ana@mail.com", "WrongPassword")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Credenciales invalidas");
    }
}
