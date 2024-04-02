package com.marindulja.mentalhealthbackend.common;

import com.marindulja.mentalhealthbackend.exceptions.UnauthorizedException;
import com.marindulja.mentalhealthbackend.models.PatientProfile;
import com.marindulja.mentalhealthbackend.models.User;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class Utilities {

    public static Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return Optional.of((User) authentication.getPrincipal());
        }
        return Optional.empty();
    }

    public static boolean patientBelongsToTherapist(Long patientId, ProfileRepository userProfileRepository) throws UnauthorizedException {
        final var therapist = Utilities.getCurrentUser().get();
        final var patientProfile = userProfileRepository.findByUserId(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient with id " + patientId + "not found"));

        if (patientProfile.getUser().getTherapist() == null || !therapist.getId().equals(patientProfile.getUser().getTherapist().getId())) {
            throw new UnauthorizedException("The patient with id " + patientId + " is not the patient of the therapist with id " + therapist.getId());
        }
        return true;
    }

    public static boolean therapistBelongsToPatient(Long therapistId, ProfileRepository userProfileRepository) throws UnauthorizedException {
        final var patient = Utilities.getCurrentUser().get();
        final var patientProfile = userProfileRepository.findByUserId(patient.getId())
                .orElseThrow(() -> new EntityNotFoundException("Patient with id " + patient.getId() + "not found"));

        if (patientProfile.getUser().getTherapist() == null || !therapistId.equals(patientProfile.getUser().getTherapist().getId())) {
            throw new UnauthorizedException("The patient with id " + patient.getId() + " is not the patient of the therapist with id " + therapistId);
        }
        return true;
    }

    public static PatientProfile getPatientProfileIfBelongsToTherapist(Long userId, ProfileRepository userProfileRepository) {
        final var therapist = Utilities.getCurrentUser().get();
        final var userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Patient with id " + userId + "not found"));
        if (userProfile instanceof PatientProfile patientProfile) {
            if (patientProfile.getUser().getTherapist() == null ||
                    !therapist.getId().equals(patientProfile.getUser().getTherapist().getId())) {
                throw new UnauthorizedException("The patient with id " + userId + " is not the patient of the therapist with id " + therapist.getId());
            }
            return patientProfile;
        }
        throw new UnauthorizedException("Current User is not a patient");
    }
}
