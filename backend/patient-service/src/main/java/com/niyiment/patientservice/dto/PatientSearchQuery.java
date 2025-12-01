package com.niyiment.patientservice.dto;

import com.niyiment.patientservice.entity.Patient;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Query parameters for patient search")
public record PatientSearchQuery(
    @Schema(description = "Search by first name (partial match)")
    String firstName,

    @Schema(description = "Search by last name (partial match)")
    String lastName,

    @Schema(description = "Search by national ID (exact match)")
    String nationalId,

    @Schema(description = "Search by phone number (partial match)")
    String phone,

    @Schema(description = "Search by email (partial match)")
    String email,

    @Schema(description = "Filter by gender")
    Patient.Gender gender,

    @Schema(description = "Filter by patient type")
    Patient.PatientType patientType,

    @Schema(description = "Filter by state")
    String state,

    @Schema(description = "Filter by LGA")
    String lga,

    @Schema(description = "Filter by date of birth from")
    LocalDate dateOfBirthFrom,

    @Schema(description = "Filter by date of birth to")
    LocalDate dateOfBirthTo,

    @Schema(description = "Filter by program code")
    String programCode,

    @Schema(description = "Page number (0-indexed)", example = "0")
    Integer page,

    @Schema(description = "Page size", example = "20")
    Integer size,

    @Schema(description = "Sort field", example = "lastName")
    String sortBy,

    @Schema(description = "Sort direction (ASC or DESC)", example = "ASC")
    String sortDirection
) {
    public PatientSearchQuery {
        page = page != null ? page : 0;
        size = size != null && size > 0 ? Math.min(size, 100) : 20;
        sortBy = sortBy != null ? sortBy : "createdAt";
        sortDirection = sortDirection != null ? sortDirection.toUpperCase() : "DESC";
    }
}