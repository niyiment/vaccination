package com.niyiment.facilityservice.mapper;

import com.niyiment.facilityservice.dto.request.CreateOrganisationRequest;
import com.niyiment.facilityservice.dto.request.OrganisationCsvDto;
import com.niyiment.facilityservice.dto.request.UpdateOrganisationRequest;
import com.niyiment.facilityservice.dto.response.OrganisationResponse;
import com.niyiment.facilityservice.entity.Organisation;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OrganisationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Organisation toEntity(CreateOrganisationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Organisation entity, UpdateOrganisationRequest request);

    @Mapping(target = "parentName", ignore = true)
    OrganisationResponse toResponse(Organisation entity);

    List<OrganisationResponse> toResponseList(List<Organisation> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parentId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Organisation csvToEntity(OrganisationCsvDto csvDto);

    @Mapping(source = "parentId", target = "parentCode", ignore = true)
    OrganisationCsvDto entityToCsv(Organisation entity);
}