package com.niyiment.patientservice.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Standard error response")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    @Schema(description = "Error code", example = "VALIDATION_ERROR")
    String code,

    @Schema(description = "Error message", example = "Invalid input data")
    String message,

    @Schema(description = "Detailed error description")
    String details,

    @Schema(description = "List of validation errors")
    List<FieldError> fieldErrors,

    @Schema(description = "Error timestamp")
    LocalDateTime timestamp,

    @Schema(description = "Request path")
    String path
) {
    public ErrorResponse(String code, String message) {
        this(code, message, null, null, LocalDateTime.now(), null);
    }

    public ErrorResponse(String code, String message, String details) {
        this(code, message, details, null, LocalDateTime.now(), null);
    }

    public ErrorResponse(String code, String message, List<FieldError> fieldErrors, String path) {
        this(code, message, null, fieldErrors, LocalDateTime.now(), path);
    }

    public record FieldError(
        @Schema(description = "Field name with error")
        String field,

        @Schema(description = "Rejected value")
        Object rejectedValue,

        @Schema(description = "Error message")
        String message
    ) {}
}