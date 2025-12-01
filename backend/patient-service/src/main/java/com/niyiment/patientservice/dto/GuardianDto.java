package com.niyiment.patientservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Guardian data transfer object")
public record GuardianDto(
    @Schema(description = "Guardian unique identifier")
    UUID id,

    @Schema(description = "Guardian name", example = "Jane Doe")
    @NotBlank(message = "Guardian name is required")
    @Size(max = 150, message = "Name must not exceed 150 characters")
    String name,

    @Schema(description = "Relationship to patient", example = "Mother")
    @Size(max = 50, message = "Relationship must not exceed 50 characters")
    String relationship,

    @Schema(description = "Guardian phone number", example = "+234-800-9876-543")
    @Pattern(regexp = "^\\+?[0-9\\-\\s]+$", message = "Invalid phone number format")
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    String phone,

    @Schema(description = "Guardian email address", example = "jane.doe@example.com")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    String email,

    @Schema(description = "Guardian address")
    @Size(max = 500, message = "Address must not exceed 500 characters")
    String address,

    @Schema(description = "Is this the primary guardian")
    Boolean isPrimary,

    @Schema(description = "Creation timestamp")
    LocalDateTime createdAt
) {}