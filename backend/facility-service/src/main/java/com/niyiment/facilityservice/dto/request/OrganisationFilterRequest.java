package com.niyiment.facilityservice.dto.request;


import com.niyiment.facilityservice.enums.OrganisationType;

import java.util.UUID;

public record OrganisationFilterRequest(
    String name,
    String code,
    UUID parentId,
    OrganisationType organisationType,
    String address
) {}