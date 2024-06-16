package com.marindulja.mentalhealthbackend.services.auth;

import com.marindulja.mentalhealthbackend.dtos.auth.JwtAuthenticationResponse;
import com.marindulja.mentalhealthbackend.dtos.auth.SignInRequest;
import com.marindulja.mentalhealthbackend.dtos.auth.SignUpRequest;

public interface AuthenticationService {

    JwtAuthenticationResponse signUp(SignUpRequest request);

    JwtAuthenticationResponse signIn(SignInRequest request);
}
