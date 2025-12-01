package com.niyiment.patientservice.common;

/**
 * Represents an error in the Result pattern.
 */
public record ResultError(String code, String message) {

    public static ResultError notFound(String entity, String identifier) {
        return new ResultError(
            "NOT_FOUND",
            String.format("%s not found with identifier: %s", entity, identifier)
        );
    }

    public static ResultError validation(String message) {
        return new ResultError("VALIDATION_ERROR", message);
    }

    public static ResultError conflict(String message) {
        return new ResultError("CONFLICT", message);
    }

    public static ResultError internal(String message) {
        return new ResultError("INTERNAL_ERROR", message);
    }

    public static ResultError businessRule(String message) {
        return new ResultError("BUSINESS_RULE_VIOLATION", message);
    }
}