package com.niyiment.facilityservice;

import com.niyiment.facilityservice.dto.request.CreateOrganisationRequest;
import com.niyiment.facilityservice.dto.request.OrganisationFilterRequest;
import com.niyiment.facilityservice.dto.request.UpdateOrganisationRequest;
import com.niyiment.facilityservice.dto.response.OrganisationResponse;
import com.niyiment.facilityservice.entity.Organisation;
import com.niyiment.facilityservice.enums.OrganisationType;
import com.niyiment.facilityservice.exception.DuplicateResourceException;
import com.niyiment.facilityservice.exception.InvalidOperationException;
import com.niyiment.facilityservice.exception.ResourceNotFoundException;
import com.niyiment.facilityservice.mapper.OrganisationMapper;
import com.niyiment.facilityservice.repository.OrganisationRepository;
import com.niyiment.facilityservice.service.OrganisationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganisationServiceTest {

    @Mock
    private OrganisationRepository organisationRepository;

    @Mock
    private OrganisationMapper organisationMapper;

    @InjectMocks
    private OrganisationService organisationService;

    private Organisation testState;
    private Organisation testLga;
    private Organisation testFacility;
    private UUID stateId;
    private UUID lgaId;
    private UUID facilityId;

    @BeforeEach
    void setUp() {
        stateId = UUID.randomUUID();
        lgaId = UUID.randomUUID();
        facilityId = UUID.randomUUID();

        testState = Organisation.builder()
            .id(stateId)
            .name("Lagos State")
            .code("NG-LA")
            .organisationType(OrganisationType.STATE)
            .address("Ikeja, Lagos")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        testLga = Organisation.builder()
            .id(lgaId)
            .name("Ikeja LGA")
            .code("NG-LA-IKE")
            .parentId(stateId)
            .organisationType(OrganisationType.LGA)
            .address("Ikeja, Lagos")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        testFacility = Organisation.builder()
            .id(facilityId)
            .name("LASUTH")
            .code("FAC-LA-LASUTH")
            .parentId(lgaId)
            .organisationType(OrganisationType.FACILITY)
            .address("Ikeja, Lagos")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Test
    void create_WithValidStateRequest_ShouldCreateSuccessfully() {
        CreateOrganisationRequest request = new CreateOrganisationRequest(
            "Lagos State",
            "NG-LA",
            null,
            OrganisationType.STATE,
            "Ikeja, Lagos"
        );

        OrganisationResponse expectedResponse = new OrganisationResponse(
            stateId, "Lagos State", "NG-LA", null, null,
            OrganisationType.STATE, "Ikeja, Lagos",
            LocalDateTime.now(), LocalDateTime.now()
        );

        when(organisationRepository.existsByCode(request.code())).thenReturn(false);
        when(organisationMapper.toEntity(request)).thenReturn(testState);
        when(organisationRepository.save(any(Organisation.class))).thenReturn(testState);
        when(organisationMapper.toResponse(testState)).thenReturn(expectedResponse);

        OrganisationResponse result = organisationService.create(request);

        assertNotNull(result);
        assertEquals("Lagos State", result.name());
        assertEquals(OrganisationType.STATE, result.organisationType());
        verify(organisationRepository).save(any(Organisation.class));
    }

    @Test
    void create_WithDuplicateCode_ShouldThrowDuplicateResourceException() {
        CreateOrganisationRequest request = new CreateOrganisationRequest(
            "Lagos State",
            "NG-LA",
            null,
            OrganisationType.STATE,
            "Ikeja, Lagos"
        );

        when(organisationRepository.existsByCode(request.code())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> organisationService.create(request));
        verify(organisationRepository, never()).save(any(Organisation.class));
    }

    @Test
    void create_WithLgaWithoutStateParent_ShouldThrowInvalidOperationException() {
        CreateOrganisationRequest request = new CreateOrganisationRequest(
            "Ikeja LGA",
            "NG-LA-IKE",
            null,
            OrganisationType.LGA,
            "Ikeja, Lagos"
        );

        when(organisationRepository.existsByCode(request.code())).thenReturn(false);

        assertThrows(InvalidOperationException.class, () -> organisationService.create(request));
        verify(organisationRepository, never()).save(any(Organisation.class));
    }

    @Test
    void getById_WithValidId_ShouldReturnOrganisation() {
        OrganisationResponse expectedResponse = new OrganisationResponse(
            stateId, "Lagos State", "NG-LA", null, null,
            OrganisationType.STATE, "Ikeja, Lagos",
            LocalDateTime.now(), LocalDateTime.now()
        );

        when(organisationRepository.findById(stateId)).thenReturn(Optional.of(testState));
        when(organisationMapper.toResponse(testState)).thenReturn(expectedResponse);

        OrganisationResponse result = organisationService.getById(stateId);

        assertNotNull(result);
        assertEquals("Lagos State", result.name());
        verify(organisationRepository).findById(stateId);
    }

    @Test
    void getById_WithInvalidId_ShouldThrowResourceNotFoundException() {
        UUID invalidId = UUID.randomUUID();
        when(organisationRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> organisationService.getById(invalidId));
    }

    @Test
    void getAll_ShouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Organisation> page = new PageImpl<>(List.of(testState), pageable, 1);

        when(organisationRepository.findAll(pageable)).thenReturn(page);
        when(organisationMapper.toResponse(testState)).thenReturn(
            new OrganisationResponse(stateId, "Lagos State", "NG-LA", null, null,
                OrganisationType.STATE, "Ikeja, Lagos", LocalDateTime.now(), LocalDateTime.now())
        );

        Page<OrganisationResponse> result = organisationService.getAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Lagos State", result.getContent().get(0).name());
    }

    @Test
    void filter_WithValidCriteria_ShouldReturnFilteredResults() {
        OrganisationFilterRequest filter = new OrganisationFilterRequest(
            "Lagos", null, null, OrganisationType.STATE, null
        );
        Pageable pageable = PageRequest.of(0, 10);
        Page<Organisation> page = new PageImpl<>(List.of(testState), pageable, 1);

        when(organisationRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(organisationMapper.toResponse(testState)).thenReturn(
            new OrganisationResponse(stateId, "Lagos State", "NG-LA", null, null,
                OrganisationType.STATE, "Ikeja, Lagos", LocalDateTime.now(), LocalDateTime.now())
        );

        Page<OrganisationResponse> result = organisationService.filter(filter, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void update_WithValidRequest_ShouldUpdateSuccessfully() {
        UpdateOrganisationRequest request = new UpdateOrganisationRequest(
            "Lago                                                                                                                                                                                                                                                                                                                                               s State Updated", "NG-LA", null, OrganisationType.STATE, "New Address"
        );

        when(organisationRepository.findById(stateId)).thenReturn(Optional.of(testState));
        when(organisationRepository.save(any(Organisation.class))).thenReturn(testState);
        when(organisationMapper.toResponse(testState)).thenReturn(
            new OrganisationResponse(stateId, "Lagos State Updated", "NG-LA", null, null,
                OrganisationType.STATE, "New Address", LocalDateTime.now(), LocalDateTime.now())
        );

        OrganisationResponse result = organisationService.update(stateId, request);

        assertNotNull(result);
        verify(organisationMapper).updateEntity(eq(testState), eq(request));
        verify(organisationRepository).save(testState);
    }

    @Test
    void delete_WithNoChildren_ShouldDeleteSuccessfully() {
        when(organisationRepository.findById(facilityId)).thenReturn(Optional.of(testFacility));
        when(organisationRepository.findByParentId(facilityId)).thenReturn(Collections.emptyList());

        organisationService.delete(facilityId);

        verify(organisationRepository).delete(testFacility);
    }

    @Test
    void delete_WithChildren_ShouldThrowInvalidOperationException() {
        when(organisationRepository.findById(stateId)).thenReturn(Optional.of(testState));
        when(organisationRepository.findByParentId(stateId)).thenReturn(List.of(testLga));

        assertThrows(InvalidOperationException.class, () -> organisationService.delete(stateId));
        verify(organisationRepository, never()).delete(any(Organisation.class));
    }

    @Test
    void getAllStates_ShouldReturnAllStates() {
        when(organisationRepository.findByOrganisationType(OrganisationType.STATE))
            .thenReturn(List.of(testState));
        when(organisationMapper.toResponseList(anyList())).thenReturn(List.of(
            new OrganisationResponse(stateId, "Lagos State", "NG-LA", null, null,
                OrganisationType.STATE, "Ikeja, Lagos", LocalDateTime.now(), LocalDateTime.now())
        ));

        List<OrganisationResponse> result = organisationService.getAllStates();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(OrganisationType.STATE, result.get(0).organisationType());
    }

    @Test
    void getLgasByState_WithValidStateId_ShouldReturnLgas() {
        when(organisationRepository.findById(stateId)).thenReturn(Optional.of(testState));
        when(organisationRepository.findByParentId(stateId)).thenReturn(List.of(testLga));
        when(organisationMapper.toResponseList(anyList())).thenReturn(List.of(
            new OrganisationResponse(lgaId, "Ikeja LGA", "NG-LA-IKE", stateId, "Lagos State",
                OrganisationType.LGA, "Ikeja, Lagos", LocalDateTime.now(), LocalDateTime.now())
        ));

        List<OrganisationResponse> result = organisationService.getLgasByState(stateId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(OrganisationType.LGA, result.get(0).organisationType());
    }

    @Test
    void getLgasByState_WithNonStateId_ShouldThrowInvalidOperationException() {
        when(organisationRepository.findById(lgaId)).thenReturn(Optional.of(testLga));

        assertThrows(InvalidOperationException.class, 
            () -> organisationService.getLgasByState(lgaId));
    }

    @Test
    void getFacilitiesByLga_WithValidLgaId_ShouldReturnFacilities() {
        when(organisationRepository.findById(lgaId)).thenReturn(Optional.of(testLga));
        when(organisationRepository.findByParentId(lgaId)).thenReturn(List.of(testFacility));
        when(organisationMapper.toResponseList(anyList())).thenReturn(List.of(
            new OrganisationResponse(facilityId, "LASUTH", "FAC-LA-LASUTH", lgaId, "Ikeja LGA",
                OrganisationType.FACILITY, "Ikeja, Lagos", LocalDateTime.now(), LocalDateTime.now())
        ));

        List<OrganisationResponse> result = organisationService.getFacilitiesByLga(lgaId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(OrganisationType.FACILITY, result.get(0).organisationType());
    }
}