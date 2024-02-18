package com.marindulja.mentalhealthbackend.dtos;

import com.marindulja.mentalhealthbackend.models.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;

    private String username;

    private String password;

    private String email;

    private Role role;
}



