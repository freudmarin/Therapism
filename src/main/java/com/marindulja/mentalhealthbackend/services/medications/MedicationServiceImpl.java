package com.marindulja.mentalhealthbackend.services.medications;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.MedicationDto;
import com.marindulja.mentalhealthbackend.exceptions.UnauthorizedException;
import com.marindulja.mentalhealthbackend.models.Medication;
import com.marindulja.mentalhealthbackend.models.User;
import com.marindulja.mentalhealthbackend.models.UserProfile;
import com.marindulja.mentalhealthbackend.repositories.MedicationRepository;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicationServiceImpl implements MedicationService {

    private final ModelMapper mapper = new ModelMapper();

    private final MedicationRepository medicationRepository;

    private final ProfileRepository userProfileRepository;

    public MedicationServiceImpl(MedicationRepository medicationRepository, ProfileRepository userProfileRepository) {
        this.medicationRepository = medicationRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public List<MedicationDto> getAllMedications() {
        return medicationRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }
    @Transactional
    public void assignMedicationsToUser(Long patientId, List<Long> medicationIds) {
        UserProfile patientProfile = getPatientProfileIfBelongsToTherapist(patientId);

        List<Medication> medications = medicationRepository.findAllById(medicationIds);

        if (medications.size() != medicationIds.size()) {
            throw new IllegalArgumentException("Some medications IDs are invalid");
        }

        // Clear current associations (if you want to replace, not just add)
        patientProfile.getMedications().clear();

        // Add the new associations
        for (Medication medication : medications) {
            patientProfile.getMedications().add(medication);
            medication.getUsers().add(patientProfile);
        }
        userProfileRepository.save(patientProfile);
        medicationRepository.saveAll(medications);// Save the user profile
    }

    @Transactional
    public void updateMedicationsToUser(Long patientId, List<Long> medicationIds) {
        UserProfile patientProfile = getPatientProfileIfBelongsToTherapist(patientId);

        List<Medication> newMedications = medicationRepository.findAllById(medicationIds);

        if (newMedications.size() != medicationIds.size()) {
            throw new IllegalArgumentException("Some medications IDs are invalid");
        }

        List<Medication> currentMedicationsList = patientProfile.getMedications();

        // Remove associations with medications that are not in the new list
        currentMedicationsList.removeIf(medication -> !newMedications.contains(medication));

        // Add associations with medications that are in the new list but not the current list
        for (Medication medication : newMedications) {
            if (!currentMedicationsList.contains(medication)) {
                currentMedicationsList.add(medication);
                if (!medication.getUsers().contains(patientProfile)) {
                    medication.getUsers().add(patientProfile);
                }
            }
        }

        userProfileRepository.save(patientProfile);
    }

    @Transactional
    public void removeMedicationsFromPatient(Long patientId, List<Long> medicationsIds) {
        UserProfile patientProfile = getPatientProfileIfBelongsToTherapist(patientId);

        List<Medication> medications = medicationRepository.findAllById(medicationsIds);
        if (medications.size() != medicationsIds.size()) {
            // handle cases where some disorder IDs are invalid
            throw new IllegalArgumentException("Some medications IDs are invalid");
        }
        for (Medication medication : medications) {
            medication.getUsers().remove(patientProfile);
        }
        patientProfile.getMedications().removeAll(medications);
        userProfileRepository.save(patientProfile);
    }

    private UserProfile getPatientProfileIfBelongsToTherapist(Long userId) {
        User therapist = Utilities.getCurrentUser().get();


        UserProfile patientProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Patient with id " + userId + "not found"));

        if (patientProfile.getUser().getTherapist() == null || !therapist.getId().equals(patientProfile.getUser().getTherapist().getId())) {
            throw new UnauthorizedException("The patient with id " + userId + " is not the patient of the therapist with id " + therapist.getId());
        }
        return patientProfile;
    }

    private MedicationDto mapToDTO(Medication medication) {
        return mapper.map(medication, MedicationDto.class);
    }

    private Medication mapToEntity(MedicationDto disorderDto) {
        return mapper.map(disorderDto, Medication.class);
    }
}
