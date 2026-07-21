package com.mariafernandes.sprintly.service;

import com.mariafernandes.sprintly.domain.RefreshToken;
import com.mariafernandes.sprintly.domain.User;
import com.mariafernandes.sprintly.dto.AuthResponse;
import com.mariafernandes.sprintly.repository.RefreshTokenRepository;
import com.mariafernandes.sprintly.repository.UserRepository;
import com.mariafernandes.sprintly.security.JwtService;
import com.mariafernandes.sprintly.security.RefreshTokenGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final AuthenticationManager authenticationManager;
    private final long refreshExpirationMs;

    public AuthService(UserRepository userRepository,
                        RefreshTokenRepository refreshTokenRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService,
                        RefreshTokenGenerator refreshTokenGenerator,
                        AuthenticationManager authenticationManager,
                        @Value("${jwt.refresh-expiration}") long refreshExpirationMs) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.authenticationManager = authenticationManager;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public AuthResponse register(String email, String rawPassword) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        User user = new User(email, passwordEncoder.encode(rawPassword));
        userRepository.save(user);
        return generateTokenPair(user);
    }

    public AuthResponse login(String email, String rawPassword) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, rawPassword)
        );
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        return generateTokenPair(user);
    }

    public AuthResponse refresh(String oldRefreshToken) {
        RefreshToken stored = refreshTokenRepository.findByToken(oldRefreshToken)
            .orElseThrow(() -> new IllegalArgumentException("Refresh token inválido"));

        if (!stored.isValid()) {
            throw new IllegalArgumentException("Refresh token expirado ou revogado");
        }

        // rotação: revoga o antigo
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        // gera novo par, com expiração deslizando pra frente
        return generateTokenPair(stored.getUser());
    }

    private AuthResponse generateTokenPair(User user) {
        String accessToken = jwtService.generateAccessToken(user);

        String refreshTokenValue = refreshTokenGenerator.generate();
        RefreshToken refreshToken = new RefreshToken(
            refreshTokenValue,
            user,
            LocalDateTime.now().plus(refreshExpirationMs, ChronoUnit.MILLIS)
        );
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(accessToken, refreshTokenValue);
    }
}
