package com.niyiment.patientservice.dto;

import com.niyiment.patientservice.entity.Patient;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Patient data transfer object")
public record PatientDto(
    @Schema(description = "Patient unique identifier")
    UUID id,

    @Schema(description = "Patient first name", example = "John")
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    String firstName,

    @Schema(description = "Patient last name", example = "Doe")
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    String lastName,

    @Schema(description = "Date of birth", example = "1990-01-15")
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    LocalDate dateOfBirth,

    @Schema(description = "Gender", example = "MALE")
    Patient.Gender gender,

    @Schema(description = "National ID number", example = "12345678901")
    @Size(max = 50, message = "National ID must not exceed 50 characters")
    String nationalId,

    @Schema(description = "Phone number", example = "+234-800-1234-567")
    @Pattern(regexp = "^\\+?[0-9\\-\\s]+$", message = "Invalid phone number format")
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    String phone,

    @Schema(description = "Email address", example = "john.doe@example.com")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    String email,

    @Schema(description = "Residential address")
    @Size(max = 500, message = "Address must not exceed 500 characters")
    String address,

    @Schema(description = "State of residence")
    @Size(max = 100, message = "State must not exceed 100 characters")
    String state,

    @Schema(description = "Local Government Area")
    @Size(max = 100, message = "LGA must not exceed 100 characters")
    String lga,

    @Schema(description = "Patient type", example = "ADULT")
    @NotNull(message = "Patient type is required")
    Patient.PatientType patientType,

    @Schema(description = "QR code for patient identification")
    String qrCode,

    @Schema(description = "List of guardians")
    List<GuardianDto> guardians,

    @Schema(description = "List of enrolled programs")
    List<PatientProgramDto> programs,

    @Schema(description = "Creation timestamp")
    LocalDateTime createdAt,

    @Schema(description = "Last update timestamp")
    LocalDateTime updatedAt
) {
    public String fullName() {
        return firstName + " " + lastName;
    }
}