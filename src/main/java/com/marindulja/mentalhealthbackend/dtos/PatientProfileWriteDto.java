package com.marindulja.mentalhealthbackend.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PatientProfileWriteDto extends UserProfileWriteDto {
    private List<Long> symptomIds;
}
