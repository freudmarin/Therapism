package com.marindulja.mentalhealthbackend.services.therapysession;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.TherapySessionDto;
import com.marindulja.mentalhealthbackend.exceptions.UnauthorizedException;
import com.marindulja.mentalhealthbackend.models.TherapySession;
import com.marindulja.mentalhealthbackend.models.User;
import com.marindulja.mentalhealthbackend.models.UserProfile;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import com.marindulja.mentalhealthbackend.repositories.TherapySessionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TherapySessionServiceImpl implements TherapySessionService {
    private final ModelMapper mapper = new ModelMapper();

    private final TherapySessionRepository therapySessionRepository;

    private final ProfileRepository userProfileRepository;

    public TherapySessionServiceImpl(TherapySessionRepository therapySessionRepository, ProfileRepository userProfileRepository) {
        this.therapySessionRepository = therapySessionRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public List<TherapySessionDto> allSessionsOfTherapist(LocalDateTime start, LocalDateTime end) {
        List<TherapySession> allSessionsOfTherapist = therapySessionRepository.getTherapySessionsByTherapistAndSessionDateBetween(Utilities.getCurrentUser().get(), start, end);
        return allSessionsOfTherapist.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public TherapySessionDto createTherapySession(Long patientId, TherapySessionDto therapySessionDto) {
        if (patientBelongsToTherapist(patientId)) {
            TherapySession newTherapySession = mapToEntity(therapySessionDto);
            newTherapySession.setTherapist(Utilities.getCurrentUser().get());
            newTherapySession.setPatient(userProfileRepository.findByUserId(patientId).get().getUser());
            newTherapySession.setTherapistNotes(therapySessionDto.getTherapistNotes());
            newTherapySession.setSessionDate(therapySessionDto.getSessionDate());
            therapySessionRepository.save(newTherapySession);
            return mapToDTO(newTherapySession);
        }
        return null;
    }

    @Override
    public TherapySessionDto updateTherapySession(Long patientId, Long therapySessionId, TherapySessionDto therapySessionDto) {
        if (patientBelongsToTherapist(patientId)) {
            TherapySession existingTherapySession = therapySessionRepository.findById(therapySessionId).orElseThrow(() -> new EntityNotFoundException("TherapySession with id " + therapySessionId + "not found"));
            existingTherapySession.setTherapist(Utilities.getCurrentUser().get());
            existingTherapySession.setPatient(userProfileRepository.findByUserId(patientId).get().getUser());
            existingTherapySession.setTherapistNotes(therapySessionDto.getTherapistNotes());
            existingTherapySession.setSessionDate(therapySessionDto.getSessionDate());
            therapySessionRepository.save(existingTherapySession);
            return mapToDTO(existingTherapySession);
        }
        return null;
    }

    @Override
    public TherapySessionDto updatePatientNotes(Long therapySessionId, TherapySessionDto therapySessionDto) {
        TherapySession existingTherapySession = therapySessionRepository.findById(therapySessionId).orElseThrow(() ->
                new EntityNotFoundException("TherapySession with id " + therapySessionId + " not found"));
        existingTherapySession.setPatientNotes(therapySessionDto.getPatientNotes());
        therapySessionRepository.save(existingTherapySession);
        return mapToDTO(existingTherapySession);
    }

    @Override
    public void deleteTherapySession(Long therapyId) {
        TherapySession existingTherapySession = therapySessionRepository.findById(therapyId).orElseThrow(() -> new EntityNotFoundException("TherapySession with id " + therapyId + "not found"));
        existingTherapySession.setDeleted(true);
        therapySessionRepository.save(existingTherapySession);
    }


    private boolean patientBelongsToTherapist(Long patientId) throws UnauthorizedException {
        User therapist = Utilities.getCurrentUser().get();


        UserProfile patientProfile = userProfileRepository.findByUserId(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient with id " + patientId + "not found"));

        if (patientProfile.getUser().getTherapist() == null || !therapist.getId().equals(patientProfile.getUser().getTherapist().getId())) {
            throw new UnauthorizedException("The patient with id " + patientId + " is not the patient of the therapist with id " + therapist.getId());
        }
        return true;
    }


    private TherapySessionDto mapToDTO(TherapySession therapySession) {
        return mapper.map(therapySession, TherapySessionDto.class);
    }

    private TherapySession mapToEntity(TherapySessionDto therapySessionDto) {
        return mapper.map(therapySessionDto, TherapySession.class);
    }

}
