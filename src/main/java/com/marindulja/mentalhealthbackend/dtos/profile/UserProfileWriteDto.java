package com.marindulja.mentalhealthbackend.dtos.profile;

import com.marindulja.mentalhealthbackend.models.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileWriteDto {
    private String phoneNumber;
    private Gender gender;
}
