package com.marindulja.mentalhealthbackend.dtos;

import com.marindulja.mentalhealthbackend.models.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {
    private String username;
    private String email;
    private String password;
    private Role role;
}
