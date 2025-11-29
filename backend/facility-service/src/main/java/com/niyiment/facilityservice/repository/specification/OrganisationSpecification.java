package com.niyiment.facilityservice.repository.specification;

import com.niyiment.facilityservice.dto.request.OrganisationFilterRequest;
import com.niyiment.facilityservice.entity.Organisation;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class OrganisationSpecification {

    private OrganisationSpecification() {
        // Private constructor to prevent instantiation
    }

    public static Specification<Organisation> filterBy(OrganisationFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(filter.name())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + filter.name().toLowerCase() + "%"
                ));
            }

            if (StringUtils.hasText(filter.code())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("code")),
                    "%" + filter.code().toLowerCase() + "%"
                ));
            }

            if (filter.parentId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("parentId"), filter.parentId()));
            }

            if (filter.organisationType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("organisationType"), filter.organisationType()));
            }

            if (StringUtils.hasText(filter.address())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("address")),
                    "%" + filter.address().toLowerCase() + "%"
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}