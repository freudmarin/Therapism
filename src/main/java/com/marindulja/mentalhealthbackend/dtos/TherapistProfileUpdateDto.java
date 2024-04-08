package com.marindulja.mentalhealthbackend.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TherapistProfileUpdateDto {
    private Integer yearsOfExperience;
    private String qualifications;
    private List<Long> specializationIds;
}
