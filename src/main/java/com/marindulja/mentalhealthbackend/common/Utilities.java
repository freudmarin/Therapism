package com.marindulja.mentalhealthbackend.common;

import com.marindulja.mentalhealthbackend.exceptions.UnauthorizedException;
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
}
