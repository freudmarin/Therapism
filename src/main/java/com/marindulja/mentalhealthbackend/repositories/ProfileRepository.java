package com.marindulja.mentalhealthbackend.repositories;

import com.marindulja.mentalhealthbackend.models.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<UserProfile, Long> {
}
