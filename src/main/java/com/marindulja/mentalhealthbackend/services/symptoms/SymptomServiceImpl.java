package com.marindulja.mentalhealthbackend.services.symptoms;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.exceptions.UnauthorizedException;
import com.marindulja.mentalhealthbackend.models.PatientProfile;
import com.marindulja.mentalhealthbackend.models.Symptom;
import com.marindulja.mentalhealthbackend.models.User;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import com.marindulja.mentalhealthbackend.repositories.SymptomRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SymptomServiceImpl {

    private final SymptomRepository symptomRepository;

    private final ProfileRepository userProfileRepository;

    @Transactional
    public void chooseSymptoms(Long patientId, List<Long> symptomIds) {
        final var currentUser = getCurrentAuthenticatedUser();
        authorizeUserAction(patientId, currentUser.getId());
        final var patientProfile = userProfileRepository.findByUserId(patientId)
                .filter(PatientProfile.class::isInstance)
                .map(PatientProfile.class::cast)
                .orElseThrow(() -> new EntityNotFoundException("Therapist profile not found for ID: " + patientId));
        List<Symptom> symptoms = symptomRepository.findAllById(symptomIds);
        if (symptoms.size() != symptomIds.size()) {
            throw new IllegalArgumentException("One or more symptoms not found.");
        }

        patientProfile.setSymptoms(symptoms);
        userProfileRepository.save(patientProfile);
    }

    private User getCurrentAuthenticatedUser() {
        return Utilities.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("No authenticated user found."));
    }

    private void authorizeUserAction(Long targetUserId, Long authenticatedUserId) {
        if (!targetUserId.equals(authenticatedUserId)) {
            throw new UnauthorizedException("User with id " + authenticatedUserId + " not authorized.");
        }
    }
}
