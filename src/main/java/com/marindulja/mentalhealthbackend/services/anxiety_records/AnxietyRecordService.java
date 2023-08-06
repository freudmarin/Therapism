package com.marindulja.mentalhealthbackend.services.anxiety_records;

import com.marindulja.mentalhealthbackend.dtos.AnxietyRecordDto;

import java.util.List;

public interface AnxietyRecordService {

    AnxietyRecordDto registerAnxietyLevels(AnxietyRecordDto anxietyRecord);

    AnxietyRecordDto getById(long id);
    List<AnxietyRecordDto> viewPatientAnxietyLevels(long patientId);
}
