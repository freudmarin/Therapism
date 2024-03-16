package com.marindulja.mentalhealthbackend.services.therapysession;

import com.marindulja.mentalhealthbackend.dtos.TherapySessionReadDto;
import com.marindulja.mentalhealthbackend.dtos.TherapySessionWriteDto;

import java.time.LocalDateTime;
import java.util.List;

public interface TherapySessionService {
    List<TherapySessionReadDto> allSessionsOfTherapist(LocalDateTime start, LocalDateTime end);
    TherapySessionReadDto createTherapySession(Long therapistId, TherapySessionWriteDto therapySessionDto);

    TherapySessionReadDto updateTherapySession(Long patientId, Long therapySessionId, TherapySessionWriteDto therapySessionDto,
    String zoomOAuthCode);

    TherapySessionReadDto updatePatientNotes(Long therapySessionId, TherapySessionWriteDto therapySessionDto);

    TherapySessionReadDto acceptSession(Long sessionId, String zoomCode);

    TherapySessionReadDto getTherapySession(Long sessionId);

    void declineSession(Long sessionId);
    void deleteTherapySession(Long therapyId);
}
