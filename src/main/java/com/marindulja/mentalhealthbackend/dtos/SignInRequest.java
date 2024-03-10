package com.marindulja.mentalhealthbackend.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class SignInRequest {
    @NotEmpty
    @Email
    private String email;
    @NotEmpty
    private String password;
}
