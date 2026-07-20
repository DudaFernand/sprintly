package com.mariafernandes.sprintly.controller;

import com.mariafernandes.sprintly.config.OpenApiConfig;
import com.mariafernandes.sprintly.dto.AuthResponse;
import com.mariafernandes.sprintly.dto.LoginRequest;
import com.mariafernandes.sprintly.dto.RefreshRequest;
import com.mariafernandes.sprintly.dto.RegisterRequest;
import com.mariafernandes.sprintly.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = OpenApiConfig.AUTH_TAG, description = "Comece por aqui: register / login / refresh")
@SecurityRequirements // público — sem Bearer
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Registrar usuário",
        description = "Retorna accessToken + refreshToken. Copie o accessToken e use em Authorize.")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request.email(), request.password()));
    }

    @Operation(summary = "Login",
        description = "Retorna accessToken + refreshToken. Copie o accessToken e use em Authorize.")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request.email(), request.password()));
    }

    @Operation(summary = "Renovar tokens",
        description = "Envia o refreshToken; recebe um novo par (rotação).")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }
}
