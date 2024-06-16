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
public class UserReadDto {
    private Long id;

    private String username;

    private String email;

    private Role role;
}



