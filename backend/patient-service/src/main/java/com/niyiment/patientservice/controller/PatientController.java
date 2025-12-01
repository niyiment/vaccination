package com.niyiment.patientservice.controller;

import com.niyiment.patientservice.common.Result;
import com.niyiment.patientservice.dto.*;
import com.niyiment.patientservice.entity.Patient;
import com.niyiment.patientservice.service.PatientCommandService;
import com.niyiment.patientservice.service.PatientQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.UUID;

/**
 * REST controller for patient management operations.
 * Supports JSON and XML content negotiation.
 */
@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Patient Management", description = "APIs for managing patient records and vaccinations")
public class PatientController {

    private final PatientCommandService commandService;
    private final PatientQueryService queryService;

    @Operation(summary = "Register a new patient", description = "Creates a new patient record with optional guardians and program enrollments")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Patient registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Patient already exists")
    })
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
                 produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<PatientDto> registerPatient(@Valid @RequestBody CreatePatientCommand command) {
        log.info("Registering new patient: {} {}", command.firstName(), command.lastName());
        
        Result<PatientDto> result = commandService.registerPatient(command);
        
        return result.isSuccess()
            ? ResponseEntity.status(HttpStatus.CREATED).body(result.getValue())
            : ResponseEntity.status(getHttpStatus(result.getError().code()))
                .body(null);
    }

    @Operation(summary = "Get patient by ID", description = "Retrieves a patient record by their unique identifier")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Patient found"),
        @ApiResponse(responseCode = "404", description = "Patient not found")
    })
    @GetMapping(value = "/{patientId}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<PatientDto> getPatientById(
        @Parameter(description = "Patient UUID") @PathVariable UUID patientId
    ) {
        log.debug("Retrieving patient: {}", patientId);
        
        Result<PatientDto> result = queryService.getPatientById(patientId);
        
        return result.isSuccess()
            ? ResponseEntity.ok(result.getValue())
            : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @Operation(summary = "Search patient by national ID", description = "Retrieves a patient record using their national identification number")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Patient found"),
        @ApiResponse(responseCode = "404", description = "Patient not found")
    })
    @GetMapping(value = "/national-id/{nationalId}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<PatientDto> getPatientByNationalId(
        @Parameter(description = "National ID number") @PathVariable String nationalId
    ) {
        log.debug("Searching patient by national ID: {}", nationalId);
        
        Result<PatientDto> result = queryService.getPatientByNationalId(nationalId);
        
        return result.isSuccess()
            ? ResponseEntity.ok(result.getValue())
            : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @Operation(summary = "Search patient by QR code", description = "Retrieves a patient record by scanning their QR code")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Patient found"),
        @ApiResponse(responseCode = "400", description = "Invalid QR code"),
        @ApiResponse(responseCode = "404", description = "Patient not found")
    })
    @GetMapping(value = "/qr-code", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<PatientDto> getPatientByQRCode(
        @Parameter(description = "QR code data") @RequestParam String qrData
    ) {
        log.debug("Searching patient by QR code");
        
        Result<PatientDto> result = queryService.getPatientByQRCode(qrData);
        
        return result.isSuccess()
            ? ResponseEntity.ok(result.getValue())
            : ResponseEntity.status(getHttpStatus(result.getError().code())).build();
    }

    @Operation(summary = "Update patient information", description = "Updates existing patient record information")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Patient updated successfully"),
        @ApiResponse(responseCode = "404", description = "Patient not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping(value = "/{patientId}",
                consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
                produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<PatientDto> updatePatient(
        @Parameter(description = "Patient UUID") @PathVariable UUID patientId,
        @Valid @RequestBody UpdatePatientCommand command
    ) {
        log.info("Updating patient: {}", patientId);
        
        Result<PatientDto> result = commandService.updatePatient(patientId, command);
        
        return result.isSuccess()
            ? ResponseEntity.ok(result.getValue())
            : ResponseEntity.status(getHttpStatus(result.getError().code())).build();
    }

    @Operation(summary = "Search patients", description = "Search and filter patients with pagination")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Page<PatientDto>> searchPatients(
        @Parameter(description = "First name filter") @RequestParam(required = false) String firstName,
        @Parameter(description = "Last name filter") @RequestParam(required = false) String lastName,
        @Parameter(description = "National ID filter") @RequestParam(required = false) String nationalId,
        @Parameter(description = "Phone filter") @RequestParam(required = false) String phone,
        @Parameter(description = "Email filter") @RequestParam(required = false) String email,
        @Parameter(description = "Gender filter") @RequestParam(required = false) Patient.Gender gender,
        @Parameter(description = "Patient type filter") @RequestParam(required = false) Patient.PatientType patientType,
        @Parameter(description = "State filter") @RequestParam(required = false) String state,
        @Parameter(description = "LGA filter") @RequestParam(required = false) String lga,
        @Parameter(description = "Program code filter") @RequestParam(required = false) String programCode,
        @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
        @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
        @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        PatientSearchQuery query = new PatientSearchQuery(
            firstName, lastName, nationalId, phone, email, gender, patientType,
            state, lga, null, null, programCode,
            page, size, sortBy, sortDirection
        );
        
        Result<Page<PatientDto>> result = queryService.searchPatients(query);
        
        return result.isSuccess()
            ? ResponseEntity.ok(result.getValue())
            : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @Operation(summary = "Enroll patient in program", description = "Enrolls a patient in a vaccination program")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Patient enrolled successfully"),
        @ApiResponse(responseCode = "404", description = "Patient not found"),
        @ApiResponse(responseCode = "409", description = "Patient already enrolled in program")
    })
    @PostMapping("/{patientId}/programs/{programCode}")
    public ResponseEntity<PatientDto> enrollInProgram(
        @Parameter(description = "Patient UUID") @PathVariable UUID patientId,
        @Parameter(description = "Program code") @PathVariable String programCode
    ) {
        log.info("Enrolling patient {} in program {}", patientId, programCode);
        
        Result<PatientDto> result = commandService.enrollInProgram(patientId, programCode);
        
        return result.isSuccess()
            ? ResponseEntity.ok(result.getValue())
            : ResponseEntity.status(getHttpStatus(result.getError().code())).build();
    }

    @Operation(summary = "Add guardian to patient", description = "Adds a guardian to a patient record")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Guardian added successfully"),
        @ApiResponse(responseCode = "404", description = "Patient not found")
    })
    @PostMapping(value = "/{patientId}/guardians",
                 consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
                 produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<PatientDto> addGuardian(
        @Parameter(description = "Patient UUID") @PathVariable UUID patientId,
        @Valid @RequestBody CreateGuardianCommand command
    ) {
        log.info("Adding guardian to patient: {}", patientId);
        
        Result<PatientDto> result = commandService.addGuardian(patientId, command);
        
        return result.isSuccess()
            ? ResponseEntity.ok(result.getValue())
            : ResponseEntity.status(getHttpStatus(result.getError().code())).build();
    }

    @Operation(summary = "Export patient record to PDF", description = "Generates a PDF document of the patient's complete record")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "PDF generated successfully"),
        @ApiResponse(responseCode = "404", description = "Patient not found")
    })
    @GetMapping("/{patientId}/pdf")
    public ResponseEntity<byte[]> exportPatientPDF(
        @Parameter(description = "Patient UUID") @PathVariable UUID patientId
    ) {
        log.info("Generating PDF for patient: {}", patientId);
        
        Result<String> result = queryService.generatePatientPDF(patientId);
        
        if (result.isSuccess()) {
            byte[] pdfBytes = Base64.getDecoder().decode(result.getValue());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "patient-" + patientId + ".pdf");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
        }
        
        return ResponseEntity.status(getHttpStatus(result.getError().code())).build();
    }

    @Operation(summary = "Delete patient", description = "Deletes a patient record permanently")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Patient deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Patient not found")
    })
    @DeleteMapping("/{patientId}")
    public ResponseEntity<Void> deletePatient(
        @Parameter(description = "Patient UUID") @PathVariable UUID patientId
    ) {
        log.info("Deleting patient: {}", patientId);
        
        Result<Void> result = commandService.deletePatient(patientId);
        
        return result.isSuccess()
            ? ResponseEntity.noContent().build()
            : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    private HttpStatus getHttpStatus(String errorCode) {
        return switch (errorCode) {
            case "NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "VALIDATION_ERROR" -> HttpStatus.BAD_REQUEST;
            case "CONFLICT" -> HttpStatus.CONFLICT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}