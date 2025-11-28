package com.niyiment.authservice.domain.dto.response;


import lombok.Builder;

import java.time.Instant;
import java.util.Set;

@Builder

public record UserResponse(
       Long id,
       String username,
       String email,
       String firstName,
       String lastName,
       String phoneNumber,
       Boolean isEnabled,
       Boolean isLocked,
       Boolean isEmailVerified,
       Instant lastLoginAt,
       Set<RoleResponse> roles,
       Instant createdAt,
       Instant updatedAt
) {

}
