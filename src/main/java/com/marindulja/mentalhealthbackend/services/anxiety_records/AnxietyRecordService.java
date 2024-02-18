package com.marindulja.mentalhealthbackend.services.anxiety_records;

import com.marindulja.mentalhealthbackend.dtos.AnxietyRecordDto;
import com.marindulja.mentalhealthbackend.dtos.UserProfileWithUserDto;

import java.util.List;

public interface AnxietyRecordService {

    UserProfileWithUserDto registerAnxietyLevels(AnxietyRecordDto anxietyRecord);

    List<AnxietyRecordDto> getAllOfCurrentUser();
    List<AnxietyRecordDto> viewPatientAnxietyLevels(long patientId);
}
