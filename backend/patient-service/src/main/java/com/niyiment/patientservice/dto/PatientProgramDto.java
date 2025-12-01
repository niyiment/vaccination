package com.niyiment.patientservice.dto;

import com.niyiment.patientservice.entity.PatientProgram;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Patient program enrollment data transfer object")
public record PatientProgramDto(
    @Schema(description = "Program enrollment unique identifier")
    UUID id,

    @Schema(description = "Program code", example = "EPI-2024")
    @NotBlank(message = "Program code is required")
    @Size(max = 100, message = "Program code must not exceed 100 characters")
    String programCode,

    @Schema(description = "Program name", example = "Expanded Program on Immunization")
    @Size(max = 200, message = "Program name must not exceed 200 characters")
    String programName,

    @Schema(description = "Program enrollment status", example = "ACTIVE")
    PatientProgram.ProgramStatus status,

    @Schema(description = "Enrollment date")
    LocalDateTime enrolledAt,

    @Schema(description = "Completion date")
    LocalDateTime completedAt
) {}