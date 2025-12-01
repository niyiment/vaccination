package com.niyiment.patientservice.event;

import com.niyiment.patientservice.entity.Patient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a patient is registered.
 * Used for internal Spring Events and external Kafka messaging.
 */
public record PatientRegisteredEvent(
    UUID patientId,
    String firstName,
    String lastName,
    LocalDate dateOfBirth,
    Patient.Gender gender,
    String nationalId,
    String phone,
    String email,
    Patient.PatientType patientType,
    String qrCode,
    LocalDateTime registeredAt
) {
    public static PatientRegisteredEvent from(Patient patient) {
        return new PatientRegisteredEvent(
            patient.getId(),
            patient.getFirstName(),
            patient.getLastName(),
            patient.getDateOfBirth(),
            patient.getGender(),
            patient.getNationalId(),
            patient.getPhone(),
            patient.getEmail(),
            patient.getPatientType(),
            patient.getQrCode(),
            patient.getCreatedAt()
        );
    }
}