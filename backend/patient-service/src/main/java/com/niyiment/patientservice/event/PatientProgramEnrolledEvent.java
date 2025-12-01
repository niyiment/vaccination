package com.niyiment.patientservice.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a patient is enrolled in a program.
 */
public record PatientProgramEnrolledEvent(
    UUID patientId,
    UUID programId,
    String programCode,
    String programName,
    LocalDateTime enrolledAt
) {}