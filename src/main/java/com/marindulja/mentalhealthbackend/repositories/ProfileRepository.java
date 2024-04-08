package com.marindulja.mentalhealthbackend.repositories;

import com.marindulja.mentalhealthbackend.models.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUserId(Long userId);
}
