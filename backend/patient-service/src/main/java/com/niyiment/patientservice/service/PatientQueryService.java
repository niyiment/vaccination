package com.niyiment.patientservice.service;

import com.niyiment.patientservice.common.Result;
import com.niyiment.patientservice.common.ResultError;
import com.niyiment.patientservice.dto.PatientDto;
import com.niyiment.patientservice.dto.PatientSearchQuery;
import com.niyiment.patientservice.entity.Patient;
import com.niyiment.patientservice.mapper.PatientMapper;
import com.niyiment.patientservice.repository.PatientRepository;
import com.niyiment.patientservice.util.PDFGenerator;
import com.niyiment.patientservice.util.QRCodeGenerator;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Query service handling patient read operations.
 * Implements CQRS pattern for query operations with caching.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PatientQueryService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    private final QRCodeGenerator qrCodeGenerator;
    private final PDFGenerator pdfGenerator;

    /**
     * Retrieves a patient by ID.
     */
    @Cacheable(value = "patients", key = "#patientId")
    public Result<PatientDto> getPatientById(UUID patientId) {
        return patientRepository.findById(patientId)
            .map(patientMapper::toDto)
            .map(Result::<PatientDto>success)
            .orElseGet(() -> Result.failure(ResultError.notFound("Patient", patientId.toString())));
    }

    /**
     * Retrieves a patient by national ID.
     */
    public Result<PatientDto> getPatientByNationalId(String nationalId) {
        return patientRepository.findByNationalId(nationalId)
            .map(patientMapper::toDto)
            .map(Result::<PatientDto>success)
            .orElseGet(() -> Result.failure(ResultError.notFound("Patient", "national ID: " + nationalId)));
    }

    /**
     * Retrieves a patient by scanning QR code.
     */
    public Result<PatientDto> getPatientByQRCode(String qrCodeData) {
        UUID patientId = qrCodeGenerator.extractPatientId(qrCodeData);
        
        if (patientId == null) {
            return Result.failure(ResultError.validation("Invalid QR code format"));
        }

        return patientRepository.findById(patientId)
            .map(patientMapper::toDto)
            .map(Result::<PatientDto>success)
            .orElseGet(() -> Result.failure(ResultError.notFound("Patient", "QR code")));
    }

    /**
     * Searches patients with advanced filtering.
     */
    public Result<Page<PatientDto>> searchPatients(PatientSearchQuery query) {
        try {
            Pageable pageable = createPageable(query);
            Specification<Patient> spec = createSpecification(query);
            
            Page<Patient> patients = patientRepository.findAll(spec, pageable);
            Page<PatientDto> dtoPage = patients.map(patientMapper::toDto);
            
            log.debug("Found {} patients matching search criteria", dtoPage.getTotalElements());
            return Result.success(dtoPage);
        } catch (Exception e) {
            log.error("Failed to search patients", e);
            return Result.failure(ResultError.internal("Failed to search patients: " + e.getMessage()));
        }
    }

    /**
     * Retrieves all patients enrolled in a specific program.
     */
    public Result<Page<PatientDto>> getPatientsByProgram(String programCode, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Patient> patients = patientRepository.findByProgramCode(programCode, pageable);
            Page<PatientDto> dtoPage = patients.map(patientMapper::toDto);
            
            return Result.success(dtoPage);
        } catch (Exception e) {
            log.error("Failed to retrieve patients by program", e);
            return Result.failure(ResultError.internal("Failed to retrieve patients: " + e.getMessage()));
        }
    }

    /**
     * Generates a PDF document for a patient's record.
     */
    public Result<String> generatePatientPDF(UUID patientId) {
        return patientRepository.findById(patientId)
            .map(patient -> {
                try {
                    String pdfBase64 = pdfGenerator.generatePatientRecordPDF(patient);
                    log.info("Generated PDF for patient: {}", patientId);
                    return Result.<String>success(pdfBase64);
                } catch (Exception e) {
                    log.error("Failed to generate PDF for patient: {}", patientId, e);
                    return Result.<String>failure(ResultError.internal("Failed to generate PDF: " + e.getMessage()));
                }
            })
            .orElseGet(() -> Result.failure(ResultError.notFound("Patient", patientId.toString())));
    }

    /**
     * Gets patient statistics by state.
     */
    public Result<Long> getPatientCountByState(String state) {
        try {
            Long count = patientRepository.countByState(state);
            return Result.success(count);
        } catch (Exception e) {
            log.error("Failed to count patients by state", e);
            return Result.failure(ResultError.internal("Failed to count patients: " + e.getMessage()));
        }
    }

    /**
     * Gets patient statistics by type.
     */
    public Result<Long> getPatientCountByType(Patient.PatientType patientType) {
        try {
            Long count = patientRepository.countByPatientType(patientType);
            return Result.success(count);
        } catch (Exception e) {
            log.error("Failed to count patients by type", e);
            return Result.failure(ResultError.internal("Failed to count patients: " + e.getMessage()));
        }
    }

    private Pageable createPageable(PatientSearchQuery query) {
        Sort sort = query.sortDirection().equalsIgnoreCase("ASC")
            ? Sort.by(query.sortBy()).ascending()
            : Sort.by(query.sortBy()).descending();
        
        return PageRequest.of(query.page(), query.size(), sort);
    }

    private Specification<Patient> createSpecification(PatientSearchQuery query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query.firstName() != null && !query.firstName().isBlank()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("firstName")),
                    "%" + query.firstName().toLowerCase() + "%"
                ));
            }

            if (query.lastName() != null && !query.lastName().isBlank()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("lastName")),
                    "%" + query.lastName().toLowerCase() + "%"
                ));
            }

            if (query.nationalId() != null && !query.nationalId().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("nationalId"), query.nationalId()));
            }

            if (query.phone() != null && !query.phone().isBlank()) {
                predicates.add(criteriaBuilder.like(root.get("phone"), "%" + query.phone() + "%"));
            }

            if (query.email() != null && !query.email().isBlank()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("email")),
                    "%" + query.email().toLowerCase() + "%"
                ));
            }

            if (query.gender() != null) {
                predicates.add(criteriaBuilder.equal(root.get("gender"), query.gender()));
            }

            if (query.patientType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("patientType"), query.patientType()));
            }

            if (query.state() != null && !query.state().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("state"), query.state()));
            }

            if (query.lga() != null && !query.lga().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("lga"), query.lga()));
            }

            if (query.dateOfBirthFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("dateOfBirth"),
                    query.dateOfBirthFrom()
                ));
            }

            if (query.dateOfBirthTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("dateOfBirth"),
                    query.dateOfBirthTo()
                ));
            }

            if (query.programCode() != null && !query.programCode().isBlank()) {
                predicates.add(criteriaBuilder.equal(
                    root.join("programs").get("programCode"),
                    query.programCode()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}