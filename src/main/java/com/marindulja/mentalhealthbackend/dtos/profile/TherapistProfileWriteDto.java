package com.marindulja.mentalhealthbackend.dtos.profile;

import com.marindulja.mentalhealthbackend.models.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class TherapistProfileWriteDto extends UserProfileWriteDto {
    public TherapistProfileWriteDto(String phoneNumber, Gender gender, Integer yearsOfExperience, String qualifications, List<Long> specializationIds) {
        super(phoneNumber, gender);
        this.yearsOfExperience = yearsOfExperience;
        this.qualifications = qualifications;
        this.specializationIds = specializationIds;
    }

    private Integer yearsOfExperience;
    private String qualifications;
    private List<Long> specializationIds;
}
