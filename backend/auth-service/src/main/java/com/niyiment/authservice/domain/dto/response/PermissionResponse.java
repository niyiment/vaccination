package com.niyiment.authservice.domain.dto.response;


import lombok.Builder;

@Builder
public record PermissionResponse(
        Long id,
        String name,
        String description,
        String resource,
        String action
) {
}
