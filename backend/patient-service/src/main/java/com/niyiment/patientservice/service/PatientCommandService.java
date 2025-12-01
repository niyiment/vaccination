package com.niyiment.patientservice.service;


import com.niyiment.patientservice.common.Result;
import com.niyiment.patientservice.common.ResultError;
import com.niyiment.patientservice.dto.CreateGuardianCommand;
import com.niyiment.patientservice.dto.CreatePatientCommand;
import com.niyiment.patientservice.dto.PatientDto;
import com.niyiment.patientservice.dto.UpdatePatientCommand;
import com.niyiment.patientservice.entity.Guardian;
import com.niyiment.patientservice.entity.Patient;
import com.niyiment.patientservice.entity.PatientProgram;
import com.niyiment.patientservice.event.PatientProgramEnrolledEvent;
import com.niyiment.patientservice.event.PatientRegisteredEvent;
import com.niyiment.patientservice.event.PatientUpdatedEvent;
import com.niyiment.patientservice.mapper.PatientMapper;
import com.niyiment.patientservice.repository.PatientProgramRepository;
import com.niyiment.patientservice.repository.PatientRepository;
import com.niyiment.patientservice.util.QRCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

/**
 * Command service handling patient write operations.
 * Implements CQRS pattern for command operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PatientCommandService {

    private final PatientRepository patientRepository;
    private final PatientProgramRepository programRepository;
    private final PatientMapper patientMapper;
    private final QRCodeGenerator qrCodeGenerator;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Registers a new patient with validation and QR code generation.
     */
    @Transactional
    public Result<PatientDto> registerPatient(CreatePatientCommand command) {
        return validateRegistration(command)
            .flatMap(c -> createPatient(c))
            .map(patient -> {
                publishPatientRegisteredEvent(patient);
                return patientMapper.toDto(patient);
            });
    }

    /**
     * Updates existing patient information.
     */
    @Transactional
    public Result<PatientDto> updatePatient(UUID patientId, UpdatePatientCommand command) {
        return findPatientById(patientId)
            .map(patient -> updatePatientFields(patient, command))
            .map(patient -> {
                Patient saved = patientRepository.save(patient);
                publishPatientUpdatedEvent(saved);
                return patientMapper.toDto(saved);
            });
    }

    /**
     * Enrolls a patient in a vaccination program.
     */
    @Transactional
    public Result<PatientDto> enrollInProgram(UUID patientId, String programCode) {
        return findPatientById(patientId)
            .flatMap(patient -> validateProgramEnrollment(patient, programCode))
            .map(patient -> {
                PatientProgram program = createProgramEnrollment(programCode);
                patient.addProgram(program);
                Patient saved = patientRepository.save(patient);
                publishProgramEnrolledEvent(saved, program);
                return patientMapper.toDto(saved);
            });
    }

    /**
     * Adds a guardian to a patient.
     */
    @Transactional
    public Result<PatientDto> addGuardian(UUID patientId, CreateGuardianCommand command) {
        return findPatientById(patientId)
            .map(patient -> {
                Guardian guardian = patientMapper.toEntity(command);
                patient.addGuardian(guardian);
                Patient saved = patientRepository.save(patient);
                return patientMapper.toDto(saved);
            });
    }

    /**
     * Deletes a patient by ID.
     */
    @Transactional
    public Result<Void> deletePatient(UUID patientId) {
        return findPatientById(patientId)
            .map(patient -> {
                patientRepository.delete(patient);
                log.info("Deleted patient: {}", patientId);
                return null;
            });
    }

    private Result<CreatePatientCommand> validateRegistration(CreatePatientCommand command) {
        if (command.nationalId() != null && patientRepository.existsByNationalId(command.nationalId())) {
            return Result.failure(ResultError.conflict(
                "Patient with national ID " + command.nationalId() + " already exists"
            ));
        }

        if (isMinor(command.dateOfBirth())) {
            if (command.guardians() == null || command.guardians().isEmpty()) {
                return Result.failure(ResultError.validation(
                    "Guardians are required for patients under 18 years"
                ));
            }
        }

        return Result.success(command);
    }

    private Result<Patient> createPatient(CreatePatientCommand command) {
        try {
            Patient patient = patientMapper.toEntity(command);
            
            String qrCode = qrCodeGenerator.generateQRCode(UUID.randomUUID());
            patient.setQrCode(qrCode);

            if (command.guardians() != null) {
                command.guardians().forEach(guardianCmd -> {
                    Guardian guardian = patientMapper.toEntity(guardianCmd);
                    patient.addGuardian(guardian);
                });
            }

            if (command.programCodes() != null) {
                command.programCodes().forEach(code -> {
                    PatientProgram program = createProgramEnrollment(code);
                    patient.addProgram(program);
                });
            }

            Patient saved = patientRepository.save(patient);
            log.info("Registered new patient: {}", saved.getId());
            return Result.success(saved);
        } catch (Exception e) {
            log.error("Failed to create patient", e);
            return Result.failure(ResultError.internal("Failed to create patient: " + e.getMessage()));
        }
    }

    private Patient updatePatientFields(Patient patient, UpdatePatientCommand command) {
        patientMapper.updateEntityFromCommand(command, patient);
        return patient;
    }

    private Result<Patient> findPatientById(UUID patientId) {
        return patientRepository.findById(patientId)
            .map(Result::<Patient>success)
            .orElseGet(() -> Result.failure(ResultError.notFound("Patient", patientId.toString())));
    }

    private Result<Patient> validateProgramEnrollment(Patient patient, String programCode) {
        if (programRepository.existsByPatientIdAndProgramCode(patient.getId(), programCode)) {
            return Result.failure(ResultError.conflict(
                "Patient already enrolled in program: " + programCode
            ));
        }
        return Result.success(patient);
    }

    private PatientProgram createProgramEnrollment(String programCode) {
        return PatientProgram.builder()
            .programCode(programCode)
            .programName("Program: " + programCode)
            .status(PatientProgram.ProgramStatus.ACTIVE)
            .build();
    }

    private boolean isMinor(LocalDate dateOfBirth) {
        return Period.between(dateOfBirth, LocalDate.now()).getYears() < 18;
    }

    private void publishPatientRegisteredEvent(Patient patient) {
        PatientRegisteredEvent event = PatientRegisteredEvent.from(patient);
        eventPublisher.publishEvent(event);
        log.debug("Published PatientRegisteredEvent for patient: {}", patient.getId());
    }

    private void publishPatientUpdatedEvent(Patient patient) {
        PatientUpdatedEvent event = PatientUpdatedEvent.from(patient);
        eventPublisher.publishEvent(event);
        log.debug("Published PatientUpdatedEvent for patient: {}", patient.getId());
    }

    private void publishProgramEnrolledEvent(Patient patient, PatientProgram program) {
        PatientProgramEnrolledEvent event = new PatientProgramEnrolledEvent(
            patient.getId(),
            program.getId(),
            program.getProgramCode(),
            program.getProgramName(),
            program.getEnrolledAt()
        );
        eventPublisher.publishEvent(event);
        log.debug("Published PatientProgramEnrolledEvent for patient: {}", patient.getId());
    }
}