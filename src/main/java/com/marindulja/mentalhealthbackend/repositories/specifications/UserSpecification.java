package com.marindulja.mentalhealthbackend.repositories.specifications;

import com.marindulja.mentalhealthbackend.models.Role;
import com.marindulja.mentalhealthbackend.models.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification implements Specification<User> {

    private final List<Role> roles;
    private final String searchValue;

    public UserSpecification(List<Role> roles, String searchValue) {
        this.roles = roles;
        this.searchValue = searchValue;
    }

    @Override
    public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (!roles.isEmpty()) {
            CriteriaBuilder.In<Object> inClause = criteriaBuilder.in(root.get("role"));
            for (Role role : roles) {
                inClause.value(role);
            }
            predicates.add(inClause);
        }

        if (searchValue != null && !searchValue.isEmpty()) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), "%" + searchValue.toLowerCase() + "%"));
        }

        return predicates.isEmpty() ? null : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
