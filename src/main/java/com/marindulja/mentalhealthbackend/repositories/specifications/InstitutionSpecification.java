package com.marindulja.mentalhealthbackend.repositories.specifications;

import com.marindulja.mentalhealthbackend.models.Institution;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;


public class InstitutionSpecification implements Specification<Institution> {

    private final String searchValue;

    public InstitutionSpecification(String searchValue) {
        this.searchValue = searchValue;
    }

    @Override
    public Predicate toPredicate(Root<Institution> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        if (searchValue == null || searchValue.isEmpty()) {
            return criteriaBuilder.conjunction();
        }
        return criteriaBuilder.or(
                criteriaBuilder.like(root.get("name"), "%" + searchValue + "%"),
                criteriaBuilder.like(root.get("address"), "%" + searchValue + "%")
        );
    }
}
