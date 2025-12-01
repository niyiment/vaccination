package com.niyiment.patientservice.repository;

import com.niyiment.patientservice.entity.PatientProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientProgramRepository extends JpaRepository<PatientProgram, UUID> {

    List<PatientProgram> findByPatientId(UUID patientId);

    @Query("SELECT pp FROM PatientProgram pp WHERE pp.patient.id = :patientId AND pp.programCode = :programCode")
    Optional<PatientProgram> findByPatientIdAndProgramCode(
        @Param("patientId") UUID patientId,
        @Param("programCode") String programCode
    );

    @Query("SELECT pp FROM PatientProgram pp WHERE pp.patient.id = :patientId AND pp.status = :status")
    List<PatientProgram> findByPatientIdAndStatus(
        @Param("patientId") UUID patientId,
        @Param("status") PatientProgram.ProgramStatus status
    );

    boolean existsByPatientIdAndProgramCode(UUID patientId, String programCode);
}