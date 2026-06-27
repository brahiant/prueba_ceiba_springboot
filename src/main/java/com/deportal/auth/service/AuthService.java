package com.deportal.auth.service;

import com.deportal.auth.dto.AuthResponse;
import com.deportal.auth.dto.LoginRequest;
import com.deportal.auth.dto.RegisterRequest;
import com.deportal.security.JwtService;
import com.deportal.shared.exception.BusinessException;
import com.deportal.shared.exception.ResourceNotFoundException;
import com.deportal.users.dto.UserResponse;
import com.deportal.users.entity.UserEntity;
import com.deportal.users.mapper.UserMapper;
import com.deportal.users.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Ya existe un usuario con ese email");
        }

        UserEntity user = userMapper.toEntity(request, passwordEncoder.encode(request.password()));
        UserEntity savedUser = userRepository.save(user);

        return buildAuthResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmailIgnoreCase(request.email().trim().toLowerCase())
                .filter(UserEntity::isActive)
                .orElseThrow(() -> new BusinessException("Credenciales invalidas"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException("Credenciales invalidas");
        }

        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse me(String userId) {
        return userRepository.findById(userId)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("El usuario no existe"));
    }

    private AuthResponse buildAuthResponse(UserEntity user) {
        return new AuthResponse(
                jwtService.generateToken(user),
                jwtService.getExpirationSeconds(),
                userMapper.toResponse(user));
    }
}
