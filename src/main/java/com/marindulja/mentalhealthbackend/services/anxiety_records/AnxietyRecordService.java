package com.marindulja.mentalhealthbackend.services.anxiety_records;

import com.marindulja.mentalhealthbackend.dtos.AnxietyRecordReadDto;
import com.marindulja.mentalhealthbackend.dtos.AnxietyRecordWriteDto;
import com.marindulja.mentalhealthbackend.dtos.UserProfileWithUserDto;

import java.util.List;

public interface AnxietyRecordService {

    UserProfileWithUserDto registerAnxietyLevels(AnxietyRecordWriteDto anxietyRecord);

    List<AnxietyRecordReadDto> getAllOfCurrentUser();
    List<AnxietyRecordReadDto> viewPatientAnxietyLevels(long patientId);
}
