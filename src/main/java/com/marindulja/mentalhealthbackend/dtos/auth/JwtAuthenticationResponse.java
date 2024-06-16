package com.marindulja.mentalhealthbackend.dtos.auth;

import com.marindulja.mentalhealthbackend.models.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthenticationResponse {
    private String username;
    private Role role;
    private String token;
    private String refreshToken;
}
