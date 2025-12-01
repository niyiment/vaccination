package com.niyiment.patientservice.repository;

import com.niyiment.patientservice.entity.Guardian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GuardianRepository extends JpaRepository<Guardian, UUID> {

    List<Guardian> findByPatientId(UUID patientId);

    @Query("SELECT g FROM Guardian g WHERE g.patient.id = :patientId AND g.isPrimary = true")
    Optional<Guardian> findPrimaryGuardianByPatientId(@Param("patientId") UUID patientId);

    @Query("SELECT g FROM Guardian g WHERE g.phone = :phone")
    List<Guardian> findByPhone(@Param("phone") String phone);
}