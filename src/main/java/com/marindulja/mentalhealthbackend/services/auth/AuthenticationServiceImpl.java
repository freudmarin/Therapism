package com.marindulja.mentalhealthbackend.services.auth;

import com.marindulja.mentalhealthbackend.dtos.auth.JwtAuthenticationResponse;
import com.marindulja.mentalhealthbackend.dtos.auth.SignInRequest;
import com.marindulja.mentalhealthbackend.dtos.auth.SignUpRequest;
import com.marindulja.mentalhealthbackend.exceptions.AuthenticationFailedException;
import com.marindulja.mentalhealthbackend.models.User;
import com.marindulja.mentalhealthbackend.repositories.RefreshTokenRepository;
import com.marindulja.mentalhealthbackend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public JwtAuthenticationResponse signUp(SignUpRequest request) {
        final var user = User.builder().username(request.getUsername())
                .email(request.getEmail()).password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole()).build();
        userRepository.save(user);
        final var jwt = jwtService.generateToken(user);
        return JwtAuthenticationResponse.builder().username(user.getActualUsername()).role(user.getRole()).token(jwt).build();
    }

    public JwtAuthenticationResponse signIn(SignInRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (AuthenticationException e) {
            throw new AuthenticationFailedException("Invalid email or password.", e);
        }
        final var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationFailedException("Invalid email or password."));
        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);
        final var jwt = jwtService.generateToken(user);
        final var refreshToken = refreshTokenService.generateRefreshToken(user);
        return JwtAuthenticationResponse.builder()
                .username(user.getActualUsername())
                .role(user.getRole())
                .token(jwt)
                .refreshToken(refreshToken.getToken())
                .build();
    }
}
