package com.niyiment.patientservice.mapper;

import com.niyiment.patientservice.dto.*;
import com.niyiment.patientservice.entity.Guardian;
import com.niyiment.patientservice.entity.Patient;
import com.niyiment.patientservice.entity.PatientProgram;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PatientMapper {

    @Mapping(target = "guardians", source = "guardians")
    @Mapping(target = "programs", source = "programs")
    PatientDto toDto(Patient patient);

    List<PatientDto> toDtoList(List<Patient> patients);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "qrCode", ignore = true)
    @Mapping(target = "guardians", ignore = true)
    @Mapping(target = "programs", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Patient toEntity(CreatePatientCommand command);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nationalId", ignore = true)
    @Mapping(target = "patientType", ignore = true)
    @Mapping(target = "qrCode", ignore = true)
    @Mapping(target = "guardians", ignore = true)
    @Mapping(target = "programs", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromCommand(UpdatePatientCommand command, @MappingTarget Patient patient);

    // Guardian mappings
    GuardianDto toDto(Guardian guardian);

    List<GuardianDto> toGuardianDtoList(List<Guardian> guardians);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isPrimary", source = "isPrimary", defaultValue = "false")
    Guardian toEntity(CreateGuardianCommand command);

    // PatientProgram mappings
    PatientProgramDto toDto(PatientProgram program);

    List<PatientProgramDto> toProgramDtoList(List<PatientProgram> programs);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "enrolledAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    PatientProgram toEntity(String programCode);
}