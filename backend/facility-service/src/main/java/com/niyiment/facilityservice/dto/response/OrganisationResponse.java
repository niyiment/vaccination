package com.niyiment.facilityservice.dto.response;

import com.niyiment.facilityservice.enums.OrganisationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrganisationResponse(
    UUID id,
    String name,
    String code,
    UUID parentId,
    String parentName,
    OrganisationType organisationType,
    String address,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}