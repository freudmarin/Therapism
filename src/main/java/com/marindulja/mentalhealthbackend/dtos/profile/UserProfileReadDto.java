package com.marindulja.mentalhealthbackend.dtos.profile;

import com.marindulja.mentalhealthbackend.dtos.user.UserReadDto;
import com.marindulja.mentalhealthbackend.models.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileReadDto {
    private UserReadDto userDto;
    private Long profileId;
    private String phoneNumber;
    private Gender gender;
}
