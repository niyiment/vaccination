package com.niyiment.patientservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "patients", indexes = {
    @Index(name = "idx_patient_national_id", columnList = "national_id"),
    @Index(name = "idx_patient_phone", columnList = "phone"),
    @Index(name = "idx_patient_email", columnList = "email"),
    @Index(name = "idx_patient_dob", columnList = "date_of_birth")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;

    @Column(name = "national_id", length = 50, unique = true)
    private String nationalId;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "lga", length = 100)
    private String lga;

    @Enumerated(EnumType.STRING)
    @Column(name = "patient_type", nullable = false)
    private PatientType patientType;

    @Column(name = "qr_code")
    private String qrCode;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Guardian> guardians = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PatientProgram> programs = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public void addGuardian(Guardian guardian) {
        guardians.add(guardian);
        guardian.setPatient(this);
    }

    public void removeGuardian(Guardian guardian) {
        guardians.remove(guardian);
        guardian.setPatient(null);
    }

    public void addProgram(PatientProgram program) {
        programs.add(program);
        program.setPatient(this);
    }

    public void removeProgram(PatientProgram program) {
        programs.remove(program);
        program.setPatient(null);
    }

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    public enum PatientType {
        ADULT, CHILD, INFANT, ELDERLY, SPECIAL_NEEDS
    }
}