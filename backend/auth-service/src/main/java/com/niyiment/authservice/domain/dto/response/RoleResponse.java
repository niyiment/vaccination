package com.niyiment.authservice.domain.dto.response;

import lombok.Builder;

import java.util.Set;

@Builder
public record RoleResponse(
    Long id,
    String name,
    String description,
    Set<PermissionResponse> permissions
) {
}
