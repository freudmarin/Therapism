package com.marindulja.mentalhealthbackend.services.disorders;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.DisorderDto;
import com.marindulja.mentalhealthbackend.exceptions.UnauthorizedException;
import com.marindulja.mentalhealthbackend.models.Disorder;
import com.marindulja.mentalhealthbackend.models.Medication;
import com.marindulja.mentalhealthbackend.models.User;
import com.marindulja.mentalhealthbackend.models.UserProfile;
import com.marindulja.mentalhealthbackend.repositories.DisorderRepository;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DisorderServiceImpl implements DisorderService {

    private final ModelMapper mapper = new ModelMapper();

    private final DisorderRepository disorderRepository;

    private final ProfileRepository userProfileRepository;

    public DisorderServiceImpl(DisorderRepository disorderRepository, ProfileRepository userProfileRepository) {
        this.disorderRepository = disorderRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public List<DisorderDto> getAllDisorders() {
        return disorderRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }
    public void assignDisordersToUser(Long userId, List<Long> disorderIds) {
        //even the therapist can be a patient
        UserProfile patientProfile = getPatientProfileIfBelongsToTherapist(userId);

        List<Disorder> disorders = disorderRepository.findAllById(disorderIds);

        if (disorders.size() != disorderIds.size()) {
            // handle cases where some disorder IDs are invalid
            throw new IllegalArgumentException("Some disorder IDs are invalid");
        }

        patientProfile.getDisorders().addAll(disorders);
        userProfileRepository.save(patientProfile);
    }

    public void updateDisordersToUser(Long patientId, List<Long> disorderIds) {
        UserProfile patientProfile = getPatientProfileIfBelongsToTherapist(patientId);

        List<Disorder> newDisorders = disorderRepository.findAllById(disorderIds);

        if (newDisorders.size() != disorderIds.size()) {
            throw new IllegalArgumentException("Some disorders IDs are invalid");
        }

        List<Disorder> currentDisordersList = patientProfile.getDisorders();

        // Filter out medications from current list that are not in the new list
        List<Disorder> retainedDisorders = currentDisordersList.stream()
                .filter(newDisorders::contains)
                .collect(Collectors.toList());

        // Get medications from the new list that are not in the current list
        List<Disorder> disordersToAdd = newDisorders.stream()
                .filter(medication -> !retainedDisorders.contains(medication))
                .toList();

        // Combine the two lists
        retainedDisorders.addAll(disordersToAdd);

        // Set the modified medications back to the patient profile
        patientProfile.setDisorders(retainedDisorders);

        userProfileRepository.save(patientProfile);
    }

    public void removeDisordersFromPatient(Long patientId, List<Long> disorderIds) {
        UserProfile patientProfile = getPatientProfileIfBelongsToTherapist(patientId);

        List<Disorder> disorders = disorderRepository.findAllById(disorderIds);
        if (disorders.size() != disorderIds.size()) {
            // handle cases where some disorder IDs are invalid
            throw new IllegalArgumentException("Some disorder IDs are invalid");
        }
        patientProfile.getDisorders().removeAll(disorders);
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


    private DisorderDto mapToDTO(Disorder disorder) {
        return mapper.map(disorder, DisorderDto.class);
    }

    private Disorder mapToEntity(DisorderDto disorderDto) {
        return mapper.map(disorderDto, Disorder.class);
    }
}
