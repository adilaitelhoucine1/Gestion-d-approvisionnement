package com.tricol.gestionstock.controller;

import com.tricol.gestionstock.dto.auth.*;
import com.tricol.gestionstock.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT tokens")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        AuthResponseDTO response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register a new user (no role assigned by default)")
    public ResponseEntity<MessageResponseDTO> register(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        MessageResponseDTO response = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Get new access token using refresh token")
    public ResponseEntity<TokenRefreshResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request) {
        TokenRefreshResponseDTO response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get information about currently authenticated user")
    public ResponseEntity<UserInfoDTO> getCurrentUser() {
        UserInfoDTO user = authService.getCurrentUser();
        return ResponseEntity.ok(user);
    }
}

