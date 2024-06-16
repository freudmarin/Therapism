package com.marindulja.mentalhealthbackend.dtos.user;

import com.marindulja.mentalhealthbackend.models.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserWriteDto {
    private String username;

    private String password;

    private String email;

    private Role role;
}
