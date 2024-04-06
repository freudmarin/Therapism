package com.marindulja.mentalhealthbackend.services.anxiety_records;

import com.marindulja.mentalhealthbackend.dtos.AnxietyRecordReadDto;
import com.marindulja.mentalhealthbackend.dtos.AnxietyRecordWriteDto;
import com.marindulja.mentalhealthbackend.dtos.PatientProfileReadDto;

import java.util.List;

public interface AnxietyRecordService {

    PatientProfileReadDto registerAnxietyLevels(AnxietyRecordWriteDto anxietyRecord);

    List<AnxietyRecordReadDto> getAllOfCurrentPatient();
    List<AnxietyRecordReadDto> viewPatientAnxietyLevels(long patientId);
}
