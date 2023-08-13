package com.marindulja.mentalhealthbackend.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserProfileDto {
    private Long id;
    private String name;
    private String surname;
    private String phoneNumber;
    List<AnxietyRecordDto> anxietyRecords;
}
