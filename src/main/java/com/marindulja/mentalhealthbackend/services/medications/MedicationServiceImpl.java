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
    public void assignMedicationsToUser(Long patientId, List<Long> medicationIds) {
        //even the therapist can be a patient
        UserProfile patientProfile = getPatientProfileIfBelongsToTherapist(patientId);

        List<Medication> medications = medicationRepository.findAllById(medicationIds);

        if (medications.size() != medicationIds.size()) {
            // handle cases where some disorder IDs are invalid
            throw new IllegalArgumentException("Some medications IDs are invalid");
        }

        patientProfile.getMedications().addAll(medications);
        userProfileRepository.save(patientProfile);
    }

    public void updateMedicationsToUser(Long patientId, List<Long> medicationIds) {
        UserProfile patientProfile = getPatientProfileIfBelongsToTherapist(patientId);

        List<Medication> newMedications = medicationRepository.findAllById(medicationIds);

        if (newMedications.size() != medicationIds.size()) {
            throw new IllegalArgumentException("Some medications IDs are invalid");
        }

        List<Medication> currentMedicationsList = patientProfile.getMedications();

        // Filter out medications from current list that are not in the new list
        List<Medication> retainedMedications = currentMedicationsList.stream()
                .filter(newMedications::contains)
                .collect(Collectors.toList());

        // Get medications from the new list that are not in the current list
        List<Medication> medicationsToAdd = newMedications.stream()
                .filter(medication -> !retainedMedications.contains(medication))
                .toList();

        // Combine the two lists
        retainedMedications.addAll(medicationsToAdd);

        // Set the modified medications back to the patient profile
        patientProfile.setMedications(retainedMedications);

        userProfileRepository.save(patientProfile);
    }

    public void removeMedicationsFromPatient(Long patientId, List<Long> medicationsIds) {
        UserProfile patientProfile = getPatientProfileIfBelongsToTherapist(patientId);

        List<Medication> medications = medicationRepository.findAllById(medicationsIds);
        if (medications.size() != medicationsIds.size()) {
            // handle cases where some disorder IDs are invalid
            throw new IllegalArgumentException("Some medications IDs are invalid");
        }
        patientProfile.getMedications().removeAll(medications);
        userProfileRepository.save(patientProfile);
    }

    private UserProfile getPatientProfileIfBelongsToTherapist(Long userId) {
        User therapist = Utilities.getCurrentUser().get();


        UserProfile patientProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Patient with id " + userId + "not found"));

        if (!therapist.getId().equals(patientProfile.getUser().getTherapist().getId())) {
            throw new UnauthorizedException("The patient with id " + userId + "is not the patient of the therapist with id " + therapist.getId());
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
