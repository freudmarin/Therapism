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
public class UserProfileReadDto {
    private Long profileId;
    private String phoneNumber;
    private Gender gender;
    List<AnxietyRecordDto> anxietyRecords;
    List<DisorderDto> disorders;
}
