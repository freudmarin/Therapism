package com.marindulja.mentalhealthbackend.services.anxiety_records;

import com.marindulja.mentalhealthbackend.dtos.AnxietyRecordDto;
import com.marindulja.mentalhealthbackend.dtos.UserProfileDto;

import java.util.List;

public interface AnxietyRecordService {

    UserProfileDto registerAnxietyLevelsAndGetUserProfile(AnxietyRecordDto anxietyRecord);

    List<AnxietyRecordDto> getAllOfCurrentUser();
    List<AnxietyRecordDto> viewPatientAnxietyLevels(long patientId);
}
