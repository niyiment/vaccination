package com.niyiment.facilityservice.service;

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
import com.niyiment.facilityservice.repository.specification.OrganisationSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganisationService {

    private final OrganisationRepository organisationRepository;
    private final OrganisationMapper organisationMapper;

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "organisations", allEntries = true),
        @CacheEvict(value = "states", allEntries = true, condition = "#request.organisationType().name() == 'STATE'"),
        @CacheEvict(value = "lgas", allEntries = true, condition = "#request.organisationType().name() == 'LGA'"),
        @CacheEvict(value = "facilities", allEntries = true, condition = "#request.organisationType().name() == 'FACILITY'")
    })
    public OrganisationResponse create(CreateOrganisationRequest request) {
        log.info("Creating organisation with name: {}", request.name());

        validateCreate(request);
        validateParentHierarchy(request.parentId(), request.organisationType());

        Organisation organisation = organisationMapper.toEntity(request);
        Organisation saved = organisationRepository.save(organisation);
        
        log.info("Successfully created organisation with ID: {}", saved.getId());
        return enrichResponse(saved);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "organisations", key = "#id")
    public OrganisationResponse getById(UUID id) {
        log.debug("Fetching organisation with ID: {}", id);
        
        Organisation organisation = findOrganisationById(id);
        return enrichResponse(organisation);
    }

    @Transactional(readOnly = true)
    public Page<OrganisationResponse> getAll(Pageable pageable) {
        log.debug("Fetching all organisations with pagination: {}", pageable);
        
        Page<Organisation> organisations = organisationRepository.findAll(pageable);
        List<OrganisationResponse> responses = organisations.getContent().stream()
            .map(this::enrichResponse)
            .toList();
        
        return new PageImpl<>(responses, pageable, organisations.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<OrganisationResponse> filter(OrganisationFilterRequest filter, Pageable pageable) {
        log.debug("Filtering organisations with criteria: {}", filter);
        
        Specification<Organisation> spec = OrganisationSpecification.filterBy(filter);
        Page<Organisation> organisations = organisationRepository.findAll(spec, pageable);
        
        List<OrganisationResponse> responses = organisations.getContent().stream()
            .map(this::enrichResponse)
            .toList();
        
        return new PageImpl<>(responses, pageable, organisations.getTotalElements());
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "organisations", key = "#id"),
        @CacheEvict(value = "organisations", allEntries = true),
        @CacheEvict(value = "states", allEntries = true),
        @CacheEvict(value = "lgas", allEntries = true),
        @CacheEvict(value = "facilities", allEntries = true)
    })
    public OrganisationResponse update(UUID id, UpdateOrganisationRequest request) {
        log.info("Updating organisation with ID: {}", id);

        Organisation organisation = findOrganisationById(id);
        validateUpdate(organisation, request);

        if (request.parentId() != null) {
            validateParentHierarchy(request.parentId(), organisation.getOrganisationType());
            validateCircularReference(id, request.parentId());
        }

        organisationMapper.updateEntity(organisation, request);
        Organisation updated = organisationRepository.save(organisation);
        
        log.info("Successfully updated organisation with ID: {}", id);
        return enrichResponse(updated);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "organisations", key = "#id"),
        @CacheEvict(value = "organisations", allEntries = true),
        @CacheEvict(value = "states", allEntries = true),
        @CacheEvict(value = "lgas", allEntries = true),
        @CacheEvict(value = "facilities", allEntries = true)
    })
    public void delete(UUID id) {
        log.info("Deleting organisation with ID: {}", id);

        Organisation organisation = findOrganisationById(id);
        validateDelete(organisation);

        organisationRepository.delete(organisation);
        log.info("Successfully deleted organisation with ID: {}", id);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "states")
    public List<OrganisationResponse> getAllStates() {
        log.debug("Fetching all states");
        
        List<Organisation> states = organisationRepository.findByOrganisationType(OrganisationType.STATE);
        return organisationMapper.toResponseList(states);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "lgas", key = "#stateId")
    public List<OrganisationResponse> getLgasByState(UUID stateId) {
        log.debug("Fetching LGAs for state ID: {}", stateId);
        
        Organisation state = findOrganisationById(stateId);
        if (state.getOrganisationType() != OrganisationType.STATE) {
            throw new InvalidOperationException("Organisation is not a state");
        }

        List<Organisation> lgas = organisationRepository.findByParentId(stateId);
        return organisationMapper.toResponseList(lgas);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "facilities", key = "#lgaId")
    public List<OrganisationResponse> getFacilitiesByLga(UUID lgaId) {
        log.debug("Fetching facilities for LGA ID: {}", lgaId);
        
        Organisation lga = findOrganisationById(lgaId);
        if (lga.getOrganisationType() != OrganisationType.LGA) {
            throw new InvalidOperationException("Organisation is not an LGA");
        }

        List<Organisation> facilities = organisationRepository.findByParentId(lgaId);
        return organisationMapper.toResponseList(facilities);
    }

    private Organisation findOrganisationById(UUID id) {
        return organisationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Organisation", "id", id));
    }

    private void validateCreate(CreateOrganisationRequest request) {
        if (request.code() != null && organisationRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Organisation", "code", request.code());
        }
    }

    private void validateUpdate(Organisation organisation, UpdateOrganisationRequest request) {
        if (request.code() != null && !request.code().equals(organisation.getCode())) {
            if (organisationRepository.existsByCode(request.code())) {
                throw new DuplicateResourceException("Organisation", "code", request.code());
            }
        }
    }

    private void validateDelete(Organisation organisation) {
        List<Organisation> children = organisationRepository.findByParentId(organisation.getId());
        if (!children.isEmpty()) {
            throw new InvalidOperationException(
                String.format("Cannot delete organisation. It has %d child organisations", children.size())
            );
        }
    }

    private void validateParentHierarchy(UUID parentId, OrganisationType childType) {
        if (parentId == null) {
            if (childType != OrganisationType.STATE) {
                throw new InvalidOperationException("Only STATE can have null parent");
            }
            return;
        }

        Organisation parent = findOrganisationById(parentId);
        
        switch (childType) {
            case STATE -> throw new InvalidOperationException("STATE cannot have a parent");
            case LGA -> {
                if (parent.getOrganisationType() != OrganisationType.STATE) {
                    throw new InvalidOperationException("LGA must have a STATE as parent");
                }
            }
            case FACILITY -> {
                if (parent.getOrganisationType() != OrganisationType.LGA) {
                    throw new InvalidOperationException("FACILITY must have an LGA as parent");
                }
            }
        }
    }

    private void validateCircularReference(UUID organisationId, UUID parentId) {
        UUID currentParentId = parentId;
        
        while (currentParentId != null) {
            if (currentParentId.equals(organisationId)) {
                throw new InvalidOperationException("Circular reference detected in parent hierarchy");
            }
            
            Organisation parent = findOrganisationById(currentParentId);
            currentParentId = parent.getParentId();
        }
    }

    private OrganisationResponse enrichResponse(Organisation organisation) {
        OrganisationResponse baseResponse = organisationMapper.toResponse(organisation);
        
        if (organisation.getParentId() != null) {
            return organisationRepository.findById(organisation.getParentId())
                .map(parent -> new OrganisationResponse(
                    baseResponse.id(),
                    baseResponse.name(),
                    baseResponse.code(),
                    baseResponse.parentId(),
                    parent.getName(),
                    baseResponse.organisationType(),
                    baseResponse.address(),
                    baseResponse.createdAt(),
                    baseResponse.updatedAt()
                ))
                .orElse(baseResponse);
        }
        
        return baseResponse;
    }
}