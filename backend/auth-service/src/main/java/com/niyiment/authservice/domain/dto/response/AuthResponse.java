package com.niyiment.authservice.domain.dto.response;

import lombok.Builder;

import java.time.Instant;

@Builder
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        Instant expiresAt,
        UserResponse user
) {
}
