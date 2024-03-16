package com.marindulja.mentalhealthbackend.services.therapysession;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.TherapySessionReadDto;
import com.marindulja.mentalhealthbackend.dtos.TherapySessionWriteDto;
import com.marindulja.mentalhealthbackend.dtos.mapping.ModelMappingUtility;
import com.marindulja.mentalhealthbackend.integrations.zoom.*;
import com.marindulja.mentalhealthbackend.models.Role;
import com.marindulja.mentalhealthbackend.models.SessionStatus;
import com.marindulja.mentalhealthbackend.models.TherapySession;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import com.marindulja.mentalhealthbackend.repositories.TherapySessionRepository;
import com.marindulja.mentalhealthbackend.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TherapySessionServiceImpl implements TherapySessionService {

    private final ModelMappingUtility mapper;

    private final TherapySessionRepository therapySessionRepository;

    private final ProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    private final ZoomApiIntegration zoomApiIntegration;

    public TherapySessionServiceImpl(ModelMappingUtility mapper, TherapySessionRepository therapySessionRepository, ProfileRepository userProfileRepository,
                                     UserRepository userRepository, ZoomApiIntegration zoomApiIntegration) {
        this.mapper = mapper;
        this.therapySessionRepository = therapySessionRepository;
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
        this.zoomApiIntegration = zoomApiIntegration;
    }

    @Override
    public List<TherapySessionReadDto> allSessionsOfTherapist(LocalDateTime start, LocalDateTime end) {
        final var allSessionsOfTherapist = therapySessionRepository.getTherapySessionsByTherapistAndSessionDateBetween(Utilities.getCurrentUser().get(), start, end);
        return allSessionsOfTherapist.stream().map(therapySession -> mapper.map(therapySession, TherapySessionReadDto.class)).collect(Collectors.toList());
    }

    @Override
    public TherapySessionReadDto createTherapySession(Long therapistId, TherapySessionWriteDto therapySessionDto) {
        if (Utilities.therapistBelongsToPatient(therapistId, userProfileRepository)
                && checkTherapistAvailability(therapySessionDto.getSessionDate())) {
            final var newTherapySession = mapper.map(therapySessionDto, TherapySession.class);
            newTherapySession.setTherapist(userRepository.findById(therapistId).get());
            newTherapySession.setPatient(Utilities.getCurrentUser().get());
            newTherapySession.setSessionDate(therapySessionDto.getSessionDate());
            newTherapySession.setStatus(SessionStatus.REQUESTED);
            therapySessionRepository.save(newTherapySession);
            return mapper.map(newTherapySession, TherapySessionReadDto.class);
        }
        return null;
    }

    @Override
    public TherapySessionReadDto updateTherapySession(Long patientId, Long therapySessionId, TherapySessionWriteDto therapySessionDto, String zoomOAuthCode) {
        if (Utilities.patientBelongsToTherapist(patientId, userProfileRepository)) {
            final var existingTherapySession = therapySessionRepository.findById(therapySessionId).orElseThrow(() -> new EntityNotFoundException("TherapySession with id " + therapySessionId + "not found"));
            existingTherapySession.setTherapist(Utilities.getCurrentUser().get());
            existingTherapySession.setPatient(userProfileRepository.findByUserId(patientId).get().getUser());
            existingTherapySession.setTherapistNotes(therapySessionDto.getTherapistNotes());
            existingTherapySession.setSessionDate(therapySessionDto.getSessionDate());
            TokenResponse tokenResponse = null;
            try {
                tokenResponse = zoomApiIntegration.callTokenApi(zoomOAuthCode);
            } catch (IOException e) {
                log.error("Could not retrieve zoom access token");
            }
            ZoomMeetingRequest zoomMeetingRequest = new ZoomMeetingRequest();
            zoomMeetingRequest.setType(2);

            ZonedDateTime utcDateTime = therapySessionDto.getSessionDate().atZone(ZoneOffset.UTC); // Convert to UTC

            // Format for Zoom
            String zoomDateTime = utcDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));

            zoomMeetingRequest.setStartTime(zoomDateTime);
            zoomApiIntegration.callUpdateMeetingApi(zoomMeetingRequest, existingTherapySession.getMeetingId(), tokenResponse.getAccessToken());
            existingTherapySession.setStatus(SessionStatus.SCHEDULED);
            therapySessionRepository.save(existingTherapySession);
            return mapper.map(existingTherapySession, TherapySessionReadDto.class);
        }
        return null;
    }

    public TherapySessionReadDto acceptSession(Long sessionId, String zoomOAuthCode) {
        TherapySession session = therapySessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found"));
        session.setStatus(SessionStatus.SCHEDULED);
        TokenResponse tokenResponse = null;
        try {
            tokenResponse = zoomApiIntegration.callTokenApi(zoomOAuthCode);
        } catch (IOException e) {
            log.error("Could not retrieve zoom access token");
        }
        String userId = "me";
        MeetingDetailsHelper meetingDetailsHelper = new MeetingDetailsHelper();
        meetingDetailsHelper.setUserId(userId);
        ZoomMeetingRequest zoomMeetingRequest = new ZoomMeetingRequest();
        zoomMeetingRequest.setType(2);

        ZonedDateTime utcDateTime = session.getSessionDate().atZone(ZoneOffset.UTC); // Convert to UTC

        // Format for Zoom
        String zoomDateTime = utcDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));

        zoomMeetingRequest.setStartTime(zoomDateTime);
        ZoomMeetingResponse response = zoomApiIntegration.callCreateMeetingApi(meetingDetailsHelper, zoomMeetingRequest, tokenResponse.getAccessToken());
        session.setZoomStartLinkUrl(response.getStartUrl());
        session.setZoomJoinLinkUrl(response.getJoinUrl());
        session.setMeetingId(response.getMeetingId());
        therapySessionRepository.save(session);
        return mapper.map(session, TherapySessionReadDto.class);
    }

    @Override
    public TherapySessionReadDto getTherapySession(Long sessionId) {
        TherapySession session = therapySessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found"));
        if ((Utilities.getCurrentUser().get().getRole() == Role.THERAPIST &&
            Utilities.patientBelongsToTherapist(session.getTherapist().getId(), userProfileRepository)) ||
            (Utilities.getCurrentUser().get().getRole() == Role.PATIENT &&
            Utilities.therapistBelongsToPatient(session.getTherapist().getId(), userProfileRepository))) {
            return mapper.map(session, TherapySessionReadDto.class);
        }
        throw new IllegalArgumentException("Patient with id " + session.getPatient().getId()
        + "doesn't belong to therapist with id" + session.getTherapist().getId());
    }

    public void declineSession(Long sessionId) {
        TherapySession session = therapySessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found"));
        session.setStatus(SessionStatus.DECLINED);
        therapySessionRepository.save(session);
    }

    @Override
    public TherapySessionReadDto updatePatientNotes(Long therapySessionId, TherapySessionWriteDto therapySessionDto) {
        final var existingTherapySession = therapySessionRepository.findById(therapySessionId).orElseThrow(() ->
                new EntityNotFoundException("TherapySession with id " + therapySessionId + " not found"));
        existingTherapySession.setPatientNotes(therapySessionDto.getPatientNotes());
        therapySessionRepository.save(existingTherapySession);
        return mapper.map(existingTherapySession, TherapySessionReadDto.class);
    }

    @Override
    public void deleteTherapySession(Long therapyId) {
        final var existingTherapySession = therapySessionRepository.findById(therapyId).orElseThrow(() -> new EntityNotFoundException("TherapySession with id " + therapyId + "not found"));
        existingTherapySession.setDeleted(true);
        therapySessionRepository.save(existingTherapySession);
    }

    private boolean checkTherapistAvailability(LocalDateTime therapySessionTime) {
        return true;
    }
}
