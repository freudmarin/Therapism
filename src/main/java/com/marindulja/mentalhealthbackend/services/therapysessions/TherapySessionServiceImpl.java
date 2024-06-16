package com.marindulja.mentalhealthbackend.services.therapysessions;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.mapping.ModelMappingUtility;
import com.marindulja.mentalhealthbackend.dtos.therapysession.TherapySessionMoodDto;
import com.marindulja.mentalhealthbackend.dtos.therapysession.TherapySessionReadDto;
import com.marindulja.mentalhealthbackend.dtos.therapysession.TherapySessionWriteDto;
import com.marindulja.mentalhealthbackend.exceptions.UnauthorizedException;
import com.marindulja.mentalhealthbackend.integrations.zoom.*;
import com.marindulja.mentalhealthbackend.models.Role;
import com.marindulja.mentalhealthbackend.models.SessionStatus;
import com.marindulja.mentalhealthbackend.models.TherapySession;
import com.marindulja.mentalhealthbackend.models.User;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import com.marindulja.mentalhealthbackend.repositories.TherapySessionRepository;
import com.marindulja.mentalhealthbackend.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TherapySessionServiceImpl implements TherapySessionService {

    private final ModelMappingUtility mapper;

    private final TherapySessionRepository therapySessionRepository;

    private final ProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    private final ZoomApiIntegration zoomApiIntegration;

    @Override
    public List<TherapySessionReadDto> allSessionsOfTherapist(LocalDateTime start, LocalDateTime end) {
        User therapist = getCurrentUserOrThrow();
        List<TherapySession> sessions = therapySessionRepository.findTherapySessionsByTherapistAndSessionDateBetween(therapist, start, end);
        return sessions.stream()
                .map(session -> mapper.map(session, TherapySessionReadDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TherapySessionReadDto createTherapySession(Long therapistId, TherapySessionWriteDto therapySessionDto) {
        User therapist = userRepository.findById(therapistId)
                .orElseThrow(() -> new EntityNotFoundException("Therapist not found"));
        User patient = getCurrentUserOrThrow();

        if (!Utilities.therapistBelongsToPatient(therapistId, userProfileRepository)) {
            throw new UnauthorizedException("Patient is not authorized to create a session with therapist");
        }

        TherapySession newSession = mapper.map(therapySessionDto, TherapySession.class);
        newSession.setTherapist(therapist);
        newSession.setPatient(patient);
        newSession.setStatus(SessionStatus.REQUESTED);
        TherapySession savedSession = therapySessionRepository.save(newSession);

        return mapper.map(savedSession, TherapySessionReadDto.class);
    }

    @Override
    public TherapySessionReadDto updateTherapySession(Long patientId, Long therapySessionId, TherapySessionWriteDto therapySessionDto, String zoomOAuthCode) {
        if (!Utilities.patientBelongsToTherapist(patientId, userProfileRepository)) {
            throw new UnauthorizedException("Therapist is not authorized to update a session of this patient");
        }
        final var existingTherapySession = therapySessionRepository.findById(therapySessionId).orElseThrow(()
                -> new EntityNotFoundException("TherapySession with id " + therapySessionId + "not found"));
        final var patientProfile = userProfileRepository.findByUserId(patientId).orElseThrow(() ->
                new EntityNotFoundException("Patient's profile with id " + patientId + "not found"));
        existingTherapySession.setTherapist(getCurrentUserOrThrow());
        existingTherapySession.setPatient(patientProfile.getUser());
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

    public TherapySessionReadDto acceptSession(Long sessionId, String zoomOAuthCode) {
        TherapySession session = therapySessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session with id" + sessionId + "not found"));
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
        session.setMeetingId(response.getMeetingId());
        session.setZoomStartLinkUrl(response.getStartUrl());
        session.setZoomJoinLinkUrl(response.getJoinUrl());
        session.setMeetingId(response.getMeetingId());
        therapySessionRepository.save(session);
        return mapper.map(session, TherapySessionReadDto.class);
    }

    @Override
    public TherapySessionReadDto getTherapySession(Long sessionId) {
        TherapySession session = therapySessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session with id" + sessionId + "not found"));
        User currentUser = getCurrentUserOrThrow();
        validateUserRole(currentUser);
        // Validate access rights
        validateAccessToPatientData(currentUser, session.getPatient().getId());

        // If validation passes, the user has access rights, so proceed to map and return the DTO
        return mapper.map(session, TherapySessionReadDto.class);
    }

    @Override
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

    @Override
    public List<TherapySessionMoodDto> findMoodChangesAroundTherapySessions(Long patientId) {
        User currentUser = getCurrentUserOrThrow();
        validateUserRole(currentUser);
        validateAccessToPatientData(currentUser, patientId);

        List<Object[]> moodData = therapySessionRepository.findMoodChangesAroundTherapySessions(patientId);
        return moodData.stream()
                .map(this::convertToTherapySessionMoodDto)
                .collect(Collectors.toList());
    }

    private User getCurrentUserOrThrow() {
        return Utilities.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));
    }

    private void validateUserRole(User user) {
        if (user.getRole() != Role.THERAPIST && user.getRole() != Role.PATIENT) {
            throw new UnauthorizedException("The role of current user should be Therapist or Patient");
        }
    }

    private void validateAccessToPatientData(User currentUser, Long patientId) {
        if (currentUser.getRole() == Role.THERAPIST && !Utilities.patientBelongsToTherapist(patientId, userProfileRepository)) {
            throw new UnauthorizedException("Therapist does not have access to the requested patient's data");
        }
        if (currentUser.getRole() == Role.PATIENT && !currentUser.getId().equals(patientId)) {
            throw new UnauthorizedException("Patients can only access their own data");
        }
    }

    private TherapySessionMoodDto convertToTherapySessionMoodDto(Object[] data) {
        LocalDateTime sessionDate = data[1] != null ? ((Timestamp) data[1]).toLocalDateTime() : null;
        LocalDateTime nearestEntryDateBefore = data[5] != null ? ((Timestamp) data[5]).toLocalDateTime() : null;
        LocalDateTime nearestEntryDateAfter = data[6] != null ? ((Timestamp) data[6]).toLocalDateTime() : null;

        return new TherapySessionMoodDto(
                (Long) data[0], // patient_id
                sessionDate, // Converted session_date
                (Integer) data[2], // mood_before
                (Integer) data[3], // mood_day_of
                (Integer) data[4], // mood_after
                nearestEntryDateBefore, // Converted nearest_entry_date_before
                nearestEntryDateAfter  // Converted nearest_entry_date_after
        );
    }
}
