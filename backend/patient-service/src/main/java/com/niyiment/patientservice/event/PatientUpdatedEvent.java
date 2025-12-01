package com.niyiment.patientservice.event;

import com.niyiment.patientservice.entity.Patient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when patient information is updated.
 */
public record PatientUpdatedEvent(
    UUID patientId,
    String firstName,
    String lastName,
    LocalDate dateOfBirth,
    Patient.Gender gender,
    String phone,
    String email,
    String address,
    String state,
    String lga,
    LocalDateTime updatedAt
) {
    public static PatientUpdatedEvent from(Patient patient) {
        return new PatientUpdatedEvent(
            patient.getId(),
            patient.getFirstName(),
            patient.getLastName(),
            patient.getDateOfBirth(),
            patient.getGender(),
            patient.getPhone(),
            patient.getEmail(),
            patient.getAddress(),
            patient.getState(),
            patient.getLga(),
            patient.getUpdatedAt()
        );
    }
}