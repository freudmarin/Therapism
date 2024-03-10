package com.marindulja.mentalhealthbackend.dtos;

import com.marindulja.mentalhealthbackend.models.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileWithUserDto {
    UserReadDto userDto;
    private Long profileId;
    private String phoneNumber;
    private Gender gender;
    List<AnxietyRecordReadDto> anxietyRecords;
    List<DisorderDto> disorders;
}
