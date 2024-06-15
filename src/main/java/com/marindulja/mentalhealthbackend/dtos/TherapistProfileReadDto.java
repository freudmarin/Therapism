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
public class TherapistProfileReadDto extends UserProfileReadDto {
    private Integer yearsOfExperience;
    private String qualifications;
    private List<SpecializationDto> specializations;

    public TherapistProfileReadDto(UserReadDto userDto, Long profileId,
                                   String phoneNumber, Gender gender, Integer yearsOfExperience, String qualifications,
                                   List<SpecializationDto> specializations) {
        super(userDto, profileId, phoneNumber, gender);
        this.yearsOfExperience = yearsOfExperience;
        this.qualifications = qualifications;
        this.specializations = specializations;
    }
}
