package com.niyiment.facilityservice.dto.request;

import com.niyiment.facilityservice.enums.OrganisationType;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateOrganisationRequest(
    
    @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
    String name,
    
    @Size(max = 50, message = "Code must not exceed 50 characters")
    String code,
    
    UUID parentId,
    
    OrganisationType organisationType,
    
    @Size(max = 1000, message = "Address must not exceed 1000 characters")
    String address
) {}