package com.marindulja.mentalhealthbackend.services.disorders;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.DisorderDto;
import com.marindulja.mentalhealthbackend.dtos.mapping.ModelMappingUtility;
import com.marindulja.mentalhealthbackend.repositories.DisorderRepository;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DisorderServiceImpl implements DisorderService {

    private final ModelMappingUtility mapper;

    private final DisorderRepository disorderRepository;

    private final ProfileRepository userProfileRepository;

    public DisorderServiceImpl(ModelMappingUtility mapper, DisorderRepository disorderRepository, ProfileRepository userProfileRepository) {
        this.mapper = mapper;
        this.disorderRepository = disorderRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public List<DisorderDto> getAllDisorders() {
        return disorderRepository.findAll().stream().map(disorder -> mapper.map(disorder, DisorderDto.class)).collect(Collectors.toList());
    }
    @Override
    public void assignDisordersToUser(Long userId, List<Long> disorderIds) {
        //even the therapist can be a patient
        final var patientProfile = Utilities.getPatientProfileIfBelongsToTherapist(userId, userProfileRepository);
        final var disorders = disorderRepository.findAllById(disorderIds);

        if (disorders.size() != disorderIds.size()) {
            // handle cases where some disorder IDs are invalid
            throw new IllegalArgumentException("Some disorder IDs are invalid");
        }

        patientProfile.getDisorders().addAll(disorders);
        userProfileRepository.save(patientProfile);
    }

    @Override
    public void updateDisordersToUser(Long patientId, Collection<Long> disorderIds) {
        final var patientProfile = Utilities.getPatientProfileIfBelongsToTherapist(patientId, userProfileRepository);
        final var newDisorders = disorderRepository.findAllById(new HashSet<>(disorderIds));

        if (newDisorders.size() != disorderIds.size()) {
            throw new IllegalArgumentException("Some disorders IDs are invalid");
        }

        final var currentDisordersList = patientProfile.getDisorders();

        // Filter out medications from current list that are not in the new list
        final var retainedDisorders = currentDisordersList.stream()
                .filter(newDisorders::contains)
                .collect(Collectors.toList());

        // Get medications from the new list that are not in the current list
        final var disordersToAdd = newDisorders.stream()
                .filter(medication -> !retainedDisorders.contains(medication))
                .toList();

        // Combine the two lists
        retainedDisorders.addAll(disordersToAdd);

        // Set the modified medications back to the patient profile
        patientProfile.setDisorders(retainedDisorders);

        userProfileRepository.save(patientProfile);
    }

    @Override
    public void removeDisordersFromPatient(Long patientId, List<Long> disorderIds) {
        final var patientProfile = Utilities.getPatientProfileIfBelongsToTherapist(patientId, userProfileRepository);

        final var disorders = disorderRepository.findAllById(disorderIds);
        if (disorders.size() != disorderIds.size()) {
            // handle cases where some disorder IDs are invalid
            throw new IllegalArgumentException("Some disorder IDs are invalid");
        }
        patientProfile.getDisorders().removeAll(disorders);
        userProfileRepository.save(patientProfile);
    }
}
