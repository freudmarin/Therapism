package com.marindulja.mentalhealthbackend.repositories;

import com.marindulja.mentalhealthbackend.models.Disorder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DisorderRepository extends JpaRepository<Disorder, Long>, JpaSpecificationExecutor<Disorder> {
    Optional<Disorder> findByName(String name);
}
