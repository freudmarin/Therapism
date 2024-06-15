package com.marindulja.mentalhealthbackend.controllers;

import com.marindulja.mentalhealthbackend.dtos.JwtAuthenticationResponse;
import com.marindulja.mentalhealthbackend.dtos.SignInRequest;
import com.marindulja.mentalhealthbackend.dtos.SignUpRequest;
import com.marindulja.mentalhealthbackend.dtos.TokenRefreshResponse;
import com.marindulja.mentalhealthbackend.exceptions.TokenRefreshException;
import com.marindulja.mentalhealthbackend.models.RefreshToken;
import com.marindulja.mentalhealthbackend.services.auth.AuthenticationService;
import com.marindulja.mentalhealthbackend.services.auth.JwtService;
import com.marindulja.mentalhealthbackend.services.auth.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @PostMapping("signup")
    public ResponseEntity<JwtAuthenticationResponse> signup(@RequestBody @Valid SignUpRequest request) {
        return ResponseEntity.ok(authenticationService.signUp(request));
    }

    @PostMapping("signin")
    public ResponseEntity<JwtAuthenticationResponse> signIn(@RequestBody @Valid SignInRequest request) {
        return ResponseEntity.ok(authenticationService.signIn(request));
    }

    @PostMapping("refreshtoken")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> requestBody) {
        final var requestRefreshToken = requestBody.get("refreshToken");
        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtService.generateToken(user);
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not in database!"));
    }
}
