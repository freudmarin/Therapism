package com.marindulja.mentalhealthbackend.services.therapysession;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.TherapySessionDto;
import com.marindulja.mentalhealthbackend.dtos.mapping.ModelMappingUtility;
import com.marindulja.mentalhealthbackend.models.SessionStatus;
import com.marindulja.mentalhealthbackend.models.TherapySession;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import com.marindulja.mentalhealthbackend.repositories.TherapySessionRepository;
import com.marindulja.mentalhealthbackend.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TherapySessionServiceImpl implements TherapySessionService {
    private final ModelMappingUtility mapper;

    private final TherapySessionRepository therapySessionRepository;

    private final ProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    public TherapySessionServiceImpl(ModelMappingUtility mapper, TherapySessionRepository therapySessionRepository, ProfileRepository userProfileRepository,
                                     UserRepository userRepository) {
        this.mapper = mapper;
        this.therapySessionRepository = therapySessionRepository;
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<TherapySessionDto> allSessionsOfTherapist(LocalDateTime start, LocalDateTime end) {
        final var allSessionsOfTherapist = therapySessionRepository.getTherapySessionsByTherapistAndSessionDateBetween(Utilities.getCurrentUser().get(), start, end);
        return allSessionsOfTherapist.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public TherapySessionDto createTherapySession(Long therapistId, TherapySessionDto therapySessionDto) {
        if (Utilities.therapistBelongsToPatient(therapistId, userProfileRepository)
                && checkTherapistAvailability(therapySessionDto.getSessionDate())) {
            final var newTherapySession = mapToEntity(therapySessionDto);
            newTherapySession.setTherapist(userRepository.findById(therapistId).get());
            newTherapySession.setPatient(Utilities.getCurrentUser().get());
            newTherapySession.setTherapistNotes(therapySessionDto.getTherapistNotes());
            newTherapySession.setSessionDate(therapySessionDto.getSessionDate());
            therapySessionRepository.save(newTherapySession);
            return mapToDTO(newTherapySession);
        }
        return null;
    }

    @Override
    public TherapySessionDto updateTherapySession(Long patientId, Long therapySessionId, TherapySessionDto therapySessionDto) {
        if (Utilities.patientBelongsToTherapist(patientId, userProfileRepository)) {
            final var existingTherapySession = therapySessionRepository.findById(therapySessionId).orElseThrow(() -> new EntityNotFoundException("TherapySession with id " + therapySessionId + "not found"));
            existingTherapySession.setTherapist(Utilities.getCurrentUser().get());
            existingTherapySession.setPatient(userProfileRepository.findByUserId(patientId).get().getUser());
            existingTherapySession.setTherapistNotes(therapySessionDto.getTherapistNotes());
            existingTherapySession.setSessionDate(therapySessionDto.getSessionDate());
            therapySessionRepository.save(existingTherapySession);
            return mapToDTO(existingTherapySession);
        }
        return null;
    }

    public void acceptSession(Long sessionId) {
        TherapySession session = therapySessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found"));
        session.setStatus(SessionStatus.SCHEDULED);
        therapySessionRepository.save(session);
    }

    public void declineSession(Long sessionId) {
        TherapySession session = therapySessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found"));
        session.setStatus(SessionStatus.DECLINED);
        therapySessionRepository.save(session);
    }

    @Override
    public TherapySessionDto updatePatientNotes(Long therapySessionId, TherapySessionDto therapySessionDto) {
        final var existingTherapySession = therapySessionRepository.findById(therapySessionId).orElseThrow(() ->
                new EntityNotFoundException("TherapySession with id " + therapySessionId + " not found"));
        existingTherapySession.setPatientNotes(therapySessionDto.getPatientNotes());
        therapySessionRepository.save(existingTherapySession);
        return mapToDTO(existingTherapySession);
    }

    @Override
    public void deleteTherapySession(Long therapyId) {
        final var existingTherapySession = therapySessionRepository.findById(therapyId).orElseThrow(() -> new EntityNotFoundException("TherapySession with id " + therapyId + "not found"));
        existingTherapySession.setDeleted(true);
        therapySessionRepository.save(existingTherapySession);
    }

    private TherapySessionDto mapToDTO(TherapySession therapySession) {
        return mapper.map(therapySession, TherapySessionDto.class);
    }

    private TherapySession mapToEntity(TherapySessionDto therapySessionDto) {
        return mapper.map(therapySessionDto, TherapySession.class);
    }

    private boolean checkTherapistAvailability(LocalDateTime therapySessionTime) {
        return true;
    }
}
