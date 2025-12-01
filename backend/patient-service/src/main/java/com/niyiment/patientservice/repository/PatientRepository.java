package com.niyiment.patientservice.repository;

import com.niyiment.patientservice.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID>, JpaSpecificationExecutor<Patient> {

    Optional<Patient> findByNationalId(String nationalId);

    Optional<Patient> findByQrCode(String qrCode);

    boolean existsByNationalId(String nationalId);

    boolean existsByEmail(String email);

    @Query("SELECT p FROM Patient p WHERE " +
           "(:firstName IS NULL OR LOWER(p.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
           "(:lastName IS NULL OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND " +
           "(:phone IS NULL OR p.phone LIKE CONCAT('%', :phone, '%'))")
    Page<Patient> searchPatients(
        @Param("firstName") String firstName,
        @Param("lastName") String lastName,
        @Param("phone") String phone,
        Pageable pageable
    );

    @Query("SELECT p FROM Patient p WHERE p.dateOfBirth BETWEEN :startDate AND :endDate")
    Page<Patient> findByDateOfBirthBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable
    );

    @Query("SELECT p FROM Patient p JOIN p.programs pp WHERE pp.programCode = :programCode")
    Page<Patient> findByProgramCode(@Param("programCode") String programCode, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.state = :state")
    Long countByState(@Param("state") String state);

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.patientType = :patientType")
    Long countByPatientType(@Param("patientType") Patient.PatientType patientType);
}