package com.marindulja.mentalhealthbackend.dtos.profile;

import com.marindulja.mentalhealthbackend.models.Gender;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TherapistProfileWriteDto extends UserProfileWriteDto {
    public TherapistProfileWriteDto(String phoneNumber, Gender gender, Integer yearsOfExperience, String qualifications, List<Long> specializationIds) {
        super(phoneNumber, gender);
        this.yearsOfExperience = yearsOfExperience;
        this.qualifications = qualifications;
        this.specializationIds = specializationIds;
    }
    @Min(value = 1, message = "You should have at least 1 year of work experience in the field")
    @NotNull
    private Integer yearsOfExperience;

    @NotBlank(message = "You should posses some qualifications")
    private String qualifications;

    @Size(min = 1, message = "At least one specialization is required")
    private List<Long> specializationIds;
}
