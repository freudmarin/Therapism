package com.marindulja.mentalhealthbackend.services.disorders;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.DisorderDto;
import com.marindulja.mentalhealthbackend.exceptions.UnauthorizedException;
import com.marindulja.mentalhealthbackend.models.Disorder;
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

    public void deleteDisorder(Long id) {
        disorderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Disorder with id " + "was not found")).setDeleted(true);
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
