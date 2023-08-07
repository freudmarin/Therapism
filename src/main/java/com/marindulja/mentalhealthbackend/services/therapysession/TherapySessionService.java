package com.marindulja.mentalhealthbackend.services.therapysession;

import com.marindulja.mentalhealthbackend.dtos.TherapySessionDto;

import java.time.LocalDateTime;
import java.util.List;

public interface TherapySessionService {
    List<TherapySessionDto> allSessionsOfTherapist(LocalDateTime start, LocalDateTime end);
    TherapySessionDto createTherapySession(Long patientId, TherapySessionDto therapySessionDto);

    TherapySessionDto updateTherapySession(Long patientId, Long therapySessionId, TherapySessionDto therapySessionDto);

    TherapySessionDto updatePatientNotes(Long therapySessionId, TherapySessionDto therapySessionDto);
}
