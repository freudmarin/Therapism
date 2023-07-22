package com.marindulja.mentalhealthbackend.repositories;

import com.marindulja.mentalhealthbackend.models.RefreshToken;
import com.marindulja.mentalhealthbackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser(User user);
    void deleteByUserId(Long userId);
}
