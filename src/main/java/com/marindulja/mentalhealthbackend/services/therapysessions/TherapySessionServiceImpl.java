package com.marindulja.mentalhealthbackend.services.therapysessions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.mapping.DTOMappings;
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
import com.marindulja.mentalhealthbackend.services.profiles.ProfileService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class TherapySessionServiceImpl implements TherapySessionService {

    private final DTOMappings mapper;

    private final TherapySessionRepository therapySessionRepository;

    private final ProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    private final ZoomApiIntegration zoomApiIntegration;

    private final ChatClient chatClient;

    private final ObjectMapper objectMapper;

    @Qualifier("therapistProfileServiceImpl")
    private final ProfileService therapistProfileService;

    public TherapySessionServiceImpl(DTOMappings mapper, TherapySessionRepository therapySessionRepository, ProfileRepository userProfileRepository,
                                     UserRepository userRepository, ZoomApiIntegration zoomApiIntegration, ChatClient.Builder builder, ObjectMapper objectMapper,
                                     @Qualifier("therapistProfileServiceImpl") ProfileService therapistProfileService) {
        this.mapper = mapper;
        this.therapySessionRepository = therapySessionRepository;
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
        this.zoomApiIntegration = zoomApiIntegration;
        this.chatClient = builder.build();
        this.objectMapper = objectMapper;
        this.therapistProfileService = therapistProfileService;
    }

    @Override
    public List<TherapySessionReadDto> allSessionsOfTherapist(LocalDateTime start, LocalDateTime end) {
        User therapist = getCurrentUserOrThrow();
        List<TherapySession> sessions = therapySessionRepository.findTherapySessionsByTherapistAndSessionDateBetween(therapist, start, end);
        var therapySessions = sessions.stream()
                .map(mapper::toTherapySessionReadDto)
                .toList();
        therapySessions.forEach(therapySession -> {
            String message = """
                    Interpret these data of therapy session {therapySession} of this therapist {therapist}. What improvements can be made by the therapist? 
                    """;
            String therapySessionJson, therapistJson;
            try {
                therapySessionJson = objectMapper.writeValueAsString(therapySession);
                therapistJson = objectMapper.writeValueAsString(therapistProfileService.findByUserId(therapist.getId()));
            } catch (JsonProcessingException e) {
                log.error("Error processing JSON for therapy session with id {}: {}", therapySession.getId(), e.getMessage());
                return; // Skip this therapy session
            }
            var aiInterpretation = chatClient.prompt()
                    .user(u -> u.text(message).param("therapySession", therapySessionJson).param("therapist", therapistJson))
                    .call().content();
            therapySession.setAiSummary(aiInterpretation);
        });
        return therapySessions;
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

        TherapySession newSession = mapper.toTherapySession(therapySessionDto);
        newSession.setTherapist(therapist);
        newSession.setPatient(patient);
        newSession.setPatientNotes(therapySessionDto.getPatientNotes());
        newSession.setStatus(SessionStatus.REQUESTED);
        TherapySession savedSession = therapySessionRepository.save(newSession);

        return mapper.toTherapySessionReadDto(savedSession);
    }

    @Override
    public TherapySessionReadDto updateTherapySession(Long patientId, Long therapySessionId, TherapySessionWriteDto
            therapySessionDto, String zoomOAuthCode) {
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
        return mapper.toTherapySessionReadDto(existingTherapySession);
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
        return mapper.toTherapySessionReadDto(session);
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
        return mapper.toTherapySessionReadDto(session);
    }

    @Override
    public void declineSession(Long sessionId) {
        TherapySession session = therapySessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found"));
        session.setStatus(SessionStatus.DECLINED);
        therapySessionRepository.save(session);
    }

    @Override
    public void completeSession(Long sessionId) {
        TherapySession session = therapySessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found"));
        session.setStatus(SessionStatus.COMPLETED);
        therapySessionRepository.save(session);
    }

    @Override
    public TherapySessionReadDto updatePatientNotes(Long therapySessionId, TherapySessionWriteDto therapySessionDto) {
        final var existingTherapySession = therapySessionRepository.findById(therapySessionId).orElseThrow(() ->
                new EntityNotFoundException("TherapySession with id " + therapySessionId + " not found"));
        existingTherapySession.setPatientNotes(therapySessionDto.getPatientNotes());
        therapySessionRepository.save(existingTherapySession);
        return mapper.toTherapySessionReadDto(existingTherapySession);
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
                .toList();
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
