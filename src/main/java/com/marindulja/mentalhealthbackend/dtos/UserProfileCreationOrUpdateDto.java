package com.marindulja.mentalhealthbackend.dtos;

import com.marindulja.mentalhealthbackend.models.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserProfileCreationOrUpdateDto {
    private String phoneNumber;
    private Gender gender;
}
