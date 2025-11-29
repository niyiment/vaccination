package com.niyiment.facilityservice.repository;

import com.niyiment.facilityservice.entity.Organisation;
import com.niyiment.facilityservice.enums.OrganisationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganisationRepository extends JpaRepository<Organisation, UUID>,
                                                  JpaSpecificationExecutor<Organisation> {
    
    Optional<Organisation> findByCode(String code);
    
    boolean existsByCode(String code);
    
    List<Organisation> findByOrganisationType(OrganisationType type);
    
    List<Organisation> findByParentId(UUID parentId);
    
    @Query("SELECT o FROM Organisation o WHERE o.organisationType = :type AND o.parentId IS NULL")
    List<Organisation> findRootOrganisationsByType(@Param("type") OrganisationType type);
    
    long countByOrganisationType(OrganisationType type);
}