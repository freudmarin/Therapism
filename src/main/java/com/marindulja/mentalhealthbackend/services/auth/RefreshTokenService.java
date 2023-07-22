package com.marindulja.mentalhealthbackend.services.auth;

import com.marindulja.mentalhealthbackend.models.RefreshToken;
import com.marindulja.mentalhealthbackend.models.User;

import java.util.Optional;

public interface RefreshTokenService {
    RefreshToken generateRefreshToken(User user);
    Optional<RefreshToken> findByToken(String token);
    RefreshToken verifyExpiration(RefreshToken token);
}
