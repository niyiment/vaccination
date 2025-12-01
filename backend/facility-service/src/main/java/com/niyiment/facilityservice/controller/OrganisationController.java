package com.niyiment.facilityservice.controller;


import com.niyiment.facilityservice.dto.request.CreateOrganisationRequest;
import com.niyiment.facilityservice.dto.request.OrganisationFilterRequest;
import com.niyiment.facilityservice.dto.request.UpdateOrganisationRequest;
import com.niyiment.facilityservice.dto.response.OrganisationResponse;
import com.niyiment.facilityservice.entity.Organisation;
import com.niyiment.facilityservice.service.CsvService;
import com.niyiment.facilityservice.service.OrganisationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/organisations")
@RequiredArgsConstructor
@Tag(name = "Organisation Management", description = "APIs for managing healthcare organisations")
public class OrganisationController {

    private final OrganisationService organisationService;
    private final CsvService csvService;

    @PostMapping
    @Operation(summary = "Create a new organisation")
    public ResponseEntity<OrganisationResponse> create(@Valid @RequestBody CreateOrganisationRequest request) {
        OrganisationResponse response = organisationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get organisation by ID")
    public ResponseEntity<OrganisationResponse> getById(@PathVariable UUID id) {
        OrganisationResponse response = organisationService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all organisations with pagination")
    public ResponseEntity<Page<OrganisationResponse>> getAll(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<OrganisationResponse> response = organisationService.getAll(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Filter organisations with wildcard search")
    public ResponseEntity<Page<OrganisationResponse>> filter(
            @ModelAttribute OrganisationFilterRequest filter,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<OrganisationResponse> response = organisationService.filter(filter, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an organisation")
    public ResponseEntity<OrganisationResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrganisationRequest request) {
        OrganisationResponse response = organisationService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an organisation")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        organisationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/states")
    @Operation(summary = "Get all states")
    public ResponseEntity<List<OrganisationResponse>> getAllStates() {
        List<OrganisationResponse> response = organisationService.getAllStates();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/states/{stateId}/lgas")
    @Operation(summary = "Get all LGAs by state")
    public ResponseEntity<List<OrganisationResponse>> getLgasByState(@PathVariable UUID stateId) {
        List<OrganisationResponse> response = organisationService.getLgasByState(stateId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/lgas/{lgaId}/facilities")
    @Operation(summary = "Get all facilities by LGA")
    public ResponseEntity<List<OrganisationResponse>> getFacilitiesByLga(@PathVariable UUID lgaId) {
        List<OrganisationResponse> response = organisationService.getFacilitiesByLga(lgaId);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import organisations from CSV file")
    public ResponseEntity<ImportResponse> importFromCsv(@RequestParam("file") MultipartFile file) {
        List<Organisation> imported = csvService.importCsv(file);
        ImportResponse response = new ImportResponse(
            imported.size(),
            "Successfully imported organisations from CSV"
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/export", produces = "text/csv")
    @Operation(summary = "Export all organisations to CSV file")
    public ResponseEntity<Resource> exportToCsv() {
        ByteArrayInputStream csvData = csvService.exportToCsv();
        InputStreamResource resource = new InputStreamResource(csvData);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=organisations.csv");

        return ResponseEntity.ok()
            .headers(headers)
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(resource);
    }

    public record ImportResponse(int recordCount, String message) {}
}