package com.marindulja.mentalhealthbackend.services.anxiety_records;

import com.marindulja.mentalhealthbackend.dtos.anxietyrecord.AnxietyRecordReadDto;
import com.marindulja.mentalhealthbackend.dtos.anxietyrecord.AnxietyRecordWriteDto;

import java.util.List;

public interface AnxietyRecordService {

    void registerAnxietyLevels(AnxietyRecordWriteDto anxietyRecord);

    List<AnxietyRecordReadDto> getAllOfCurrentPatient();

    List<AnxietyRecordReadDto> viewPatientAnxietyLevels(long patientId);
}
