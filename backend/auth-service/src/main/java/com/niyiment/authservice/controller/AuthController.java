package com.niyiment.authservice.controller;

import com.niyiment.authservice.domain.dto.request.LoginRequest;
import com.niyiment.authservice.domain.dto.request.RefreshTokenRequest;
import com.niyiment.authservice.domain.dto.request.RegisterRequest;
import com.niyiment.authservice.domain.dto.response.AuthResponse;
import com.niyiment.authservice.domain.dto.response.UserResponse;
import com.niyiment.authservice.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.username());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for user: {}", request.username());
        UserResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest authentication) {
        log.debug("Refresh token request received");
        AuthResponse response = authService.refreshToken(authentication);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Neo4jProperties.Authentication authentication) {
        String username = authentication.getUsername();
        log.info("Logout attempt for user: {}", username);
        authService.logout(username);
        return ResponseEntity.ok().build();
    }

}
