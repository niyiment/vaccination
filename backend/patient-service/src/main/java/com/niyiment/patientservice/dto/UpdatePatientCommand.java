package com.niyiment.patientservice.dto;

import com.niyiment.patientservice.entity.Patient;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Schema(description = "Command to update patient information")
public record UpdatePatientCommand(
    @Schema(description = "Patient first name", example = "John")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    String firstName,

    @Schema(description = "Patient last name", example = "Doe")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    String lastName,

    @Schema(description = "Date of birth", example = "1990-01-15")
    @Past(message = "Date of birth must be in the past")
    LocalDate dateOfBirth,

    @Schema(description = "Gender", example = "MALE")
    Patient.Gender gender,

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
    String lga
) {}