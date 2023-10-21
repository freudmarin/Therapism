package com.marindulja.mentalhealthbackend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    private Long id;

    private String username;

    private String password;

    private String email;

    private String gender;

    private String phoneNumber;

    public UserDto(String username, String email, String gender, String phoneNumber) {
        this.username = username;
        this.email = email;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
    }
}



