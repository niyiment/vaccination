package com.niyiment.authservice.domain.mapper;

import com.niyiment.authservice.domain.dto.response.PermissionResponse;
import com.niyiment.authservice.domain.entity.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Mapper interface for Permission entity and DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PermissionMapper {

    /**
     * Map Permission entity to PermissionResponse DTO.
     */
    PermissionResponse toResponse(Permission permission);
}