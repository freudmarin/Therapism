package com.marindulja.mentalhealthbackend.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserProfileDto {
    @Getter
    @Setter
    UserDto userDto;
    private Long id;
    private String phoneNumber;
    private String gender;
    List<AnxietyRecordDto> anxietyRecords;
}
