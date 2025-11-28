package com.niyiment.authservice.domain.mapper;

import com.niyiment.authservice.domain.dto.response.RoleResponse;
import com.niyiment.authservice.domain.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Mapper interface for Role entity and DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {PermissionMapper.class})
public interface RoleMapper {

    /**
     * Map Role entity to RoleResponse DTO.
     */
    RoleResponse toResponse(Role role);
}