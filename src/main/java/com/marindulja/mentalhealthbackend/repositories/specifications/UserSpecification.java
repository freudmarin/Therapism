package com.marindulja.mentalhealthbackend.repositories.specifications;

import com.marindulja.mentalhealthbackend.models.Institution;
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

    private final Role role;
    private final String searchValue;

    private final Institution institution;

    private final User therapist;

    public UserSpecification(Role role, String searchValue, Institution institution, User therapist) {
        this.role = role;
        this.searchValue = searchValue;
        this.institution = institution;
        this.therapist = therapist;
    }

    @Override
    public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (role != null) {
            predicates.add(criteriaBuilder.equal(root.get("role"), role));
        }

        if (searchValue != null && !searchValue.isEmpty()) {
            predicates.add(criteriaBuilder.like(root.get("username"), "%" + searchValue + "%"));
        }

        if (institution != null && institution.getId() !=null) {
            predicates.add(criteriaBuilder.equal(root.get("institution").get("id"), institution.getId()));
        }

        if (therapist != null && therapist.getId() !=null) {
            predicates.add(criteriaBuilder.equal(root.get("therapist").get("therapist_id"), therapist.getId()));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
