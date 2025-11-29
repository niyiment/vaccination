package com.niyiment.facilityservice.entity;

import com.niyiment.facilityservice.enums.OrganisationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "organisation", indexes = {
        @Index(name = "idx_organisation_parent", columnList = "parent_id"),
        @Index(name = "idx_organisation_code", columnList = "code"),
        @Index(name = "idx_organisation_type", columnList = "organisation_type"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organisation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(name = "parent_id")
    private UUID parentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "organisation_type", nullable = false, length = 50)
    private OrganisationType organisationType;

    @Column(columnDefinition = "TEXT")
    private String address;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


}
