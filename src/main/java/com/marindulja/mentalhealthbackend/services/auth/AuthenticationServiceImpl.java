package com.marindulja.mentalhealthbackend.services.auth;

import com.marindulja.mentalhealthbackend.dtos.JwtAuthenticationResponse;
import com.marindulja.mentalhealthbackend.dtos.SignInRequest;
import com.marindulja.mentalhealthbackend.dtos.SignUpRequest;
import com.marindulja.mentalhealthbackend.models.Role;
import com.marindulja.mentalhealthbackend.models.User;
import com.marindulja.mentalhealthbackend.repositories.RefreshTokenRepository;
import com.marindulja.mentalhealthbackend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public JwtAuthenticationResponse signUp(SignUpRequest request) {
        var user = User.builder().username(request.getUsername())
                .email(request.getEmail()).password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole()).build();
        userRepository.save(user);
        var jwt = jwtService.generateToken(user);
        return JwtAuthenticationResponse.builder().username(user.getUsername()).role(user.getRole()).token(jwt).build();
    }

    @Override
    public JwtAuthenticationResponse signIn(SignInRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);
        var jwt = jwtService.generateToken(user);
        var refreshToken = refreshTokenService.generateRefreshToken(user);
        return JwtAuthenticationResponse.builder().username(user.getUsername()).role(user.getRole()).token(jwt)
                .refreshToken(refreshToken.getToken()).build();
    }
}
