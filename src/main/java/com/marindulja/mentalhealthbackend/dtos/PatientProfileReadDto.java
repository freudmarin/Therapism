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
public class PatientProfileReadDto extends UserProfileReadDto {
    private List<AnxietyRecordReadDto> anxietyRecords;
    private List<DisorderDto> disorders;
    private List<SymptomDto> symptoms;

    public PatientProfileReadDto(UserReadDto userDto, Long profileId, String phoneNumber, Gender gender,
                                 List<AnxietyRecordReadDto> anxietyRecords, List<DisorderDto> disorders,
                                 List<SymptomDto> symptoms) {
        super(userDto, profileId, phoneNumber, gender);
        this.anxietyRecords = anxietyRecords;
        this.disorders = disorders;
        this.symptoms = symptoms;
    }
}
