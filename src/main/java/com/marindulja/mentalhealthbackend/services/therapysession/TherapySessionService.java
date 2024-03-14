package com.marindulja.mentalhealthbackend.services.therapysession;

import com.marindulja.mentalhealthbackend.dtos.TherapySessionReadDto;
import com.marindulja.mentalhealthbackend.dtos.TherapySessionWriteDto;

import java.time.LocalDateTime;
import java.util.List;

public interface TherapySessionService {
    List<TherapySessionReadDto> allSessionsOfTherapist(LocalDateTime start, LocalDateTime end);
    TherapySessionReadDto createTherapySession(Long therapistId, TherapySessionWriteDto therapySessionDto, String zoomJoinLinkUrl);

    TherapySessionReadDto updateTherapySession(Long patientId, Long therapySessionId, TherapySessionWriteDto therapySessionDto,
    String zoomOAuthCode);

    TherapySessionReadDto updatePatientNotes(Long therapySessionId, TherapySessionWriteDto therapySessionDto);

    void acceptSession(Long sessionId);

    void declineSession(Long sessionId);
    void deleteTherapySession(Long therapyId);
}
