package com.niyiment.authservice.domain.mapper;

import com.niyiment.authservice.domain.dto.request.RegisterRequest;
import com.niyiment.authservice.domain.dto.response.UserResponse;
import com.niyiment.authservice.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * Mapper interface for User entity and DTOs.
 * Uses MapStruct for compile-time safe mappings.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {RoleMapper.class})
public interface UserMapper {

    /**
     * Map User entity to UserResponse DTO.
     */
    UserResponse toResponse(User user);

    /**
     * Map RegisterRequest DTO to User entity.
     * Excludes password hash as it needs special handling.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "isEnabled", constant = "true")
    @Mapping(target = "isLocked", constant = "false")
    @Mapping(target = "isEmailVerified", constant = "false")
    @Mapping(target = "failedLoginAttempts", constant = "0")
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "passwordChangedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    User toEntity(RegisterRequest request);
}