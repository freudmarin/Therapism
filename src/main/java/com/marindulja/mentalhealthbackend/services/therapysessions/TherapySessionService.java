package com.marindulja.mentalhealthbackend.services.therapysessions;

import com.marindulja.mentalhealthbackend.dtos.therapysession.TherapySessionMoodDto;
import com.marindulja.mentalhealthbackend.dtos.therapysession.TherapySessionReadDto;
import com.marindulja.mentalhealthbackend.dtos.therapysession.TherapySessionWriteDto;

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

    void completeSession(Long sessionId);

    void declineSession(Long sessionId);

    void deleteTherapySession(Long therapyId);

    List<TherapySessionMoodDto> findMoodChangesAroundTherapySessions(Long patientId);
}
