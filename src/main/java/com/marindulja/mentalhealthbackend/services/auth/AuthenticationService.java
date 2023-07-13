package com.marindulja.mentalhealthbackend.services.auth;

import com.marindulja.mentalhealthbackend.dtos.JwtAuthenticationResponse;
import com.marindulja.mentalhealthbackend.dtos.SignInRequest;
import com.marindulja.mentalhealthbackend.dtos.SignUpRequest;

public interface AuthenticationService {

    JwtAuthenticationResponse signUp(SignUpRequest request);

    JwtAuthenticationResponse signIn(SignInRequest request);
}
