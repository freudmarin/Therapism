package com.marindulja.mentalhealthbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserDto {
    private Long id;

    private String username;

    private String password;

    private String email;
}



