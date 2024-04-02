package com.marindulja.mentalhealthbackend.dtos;

import com.marindulja.mentalhealthbackend.models.Specialization;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TherapistProfileReadDto extends UserProfileReadDto {
    private Integer yearsOfExperience;
    private String qualifications;
    private List<Specialization> specializations;
}
