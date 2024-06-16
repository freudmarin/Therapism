package com.marindulja.mentalhealthbackend.services.profiles;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.anxietyrecord.AnxietyRecordReadDto;
import com.marindulja.mentalhealthbackend.dtos.disorder.DisorderDto;
import com.marindulja.mentalhealthbackend.dtos.mapping.ModelMappingUtility;
import com.marindulja.mentalhealthbackend.dtos.profile.PatientProfileReadDto;
import com.marindulja.mentalhealthbackend.dtos.profile.PatientProfileWriteDto;
import com.marindulja.mentalhealthbackend.dtos.profile.UserProfileReadDto;
import com.marindulja.mentalhealthbackend.dtos.profile.UserProfileWriteDto;
import com.marindulja.mentalhealthbackend.dtos.symptom.SymptomDto;
import com.marindulja.mentalhealthbackend.dtos.user.UserReadDto;
import com.marindulja.mentalhealthbackend.exceptions.UnauthorizedException;
import com.marindulja.mentalhealthbackend.models.PatientProfile;
import com.marindulja.mentalhealthbackend.models.Role;
import com.marindulja.mentalhealthbackend.models.Symptom;
import com.marindulja.mentalhealthbackend.models.User;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import com.marindulja.mentalhealthbackend.repositories.SymptomRepository;
import com.marindulja.mentalhealthbackend.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientProfileServiceImpl implements ProfileService {

    private final ModelMappingUtility mapper;
    private final SymptomRepository symptomRepository;
    private final ProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserProfileReadDto createProfile(Long userId, UserProfileWriteDto userProfileCreationDto) {
        User user = Utilities.getCurrentUser().flatMap(currentUser -> userRepository.findById(currentUser.getId()))
                .orElseThrow(() -> new UnauthorizedException("No authenticated user found"));
        authorizeUserAction(userId, user);
        PatientProfileWriteDto patientProfileDto = (PatientProfileWriteDto) userProfileCreationDto;
        PatientProfile newPatientProfile = mapper.map(patientProfileDto, PatientProfile.class);
        newPatientProfile.setUser(user);
        setPatientProfileData(newPatientProfile, patientProfileDto);
        var savedPatientProfile = userProfileRepository.save(newPatientProfile);
        return getPatientProfileReadDto(savedPatientProfile, mapper.map(savedPatientProfile.getUser(), UserReadDto.class));

    }

    @Override
    public void updateProfile(Long userId, UserProfileWriteDto userProfileUpdateDto) {
        User user = Utilities.getCurrentUser().flatMap(currentUser -> userRepository.findById(currentUser.getId()))
                .orElseThrow(() -> new UnauthorizedException("No authenticated user found"));
        authorizeUserAction(userId, user);
        PatientProfile patientProfile = userProfileRepository.findByUserId(userId)
                .filter(PatientProfile.class::isInstance)
                .map(PatientProfile.class::cast)
                .orElseThrow(() -> new EntityNotFoundException("Patient profile not found for user ID: " + userId));
        PatientProfileWriteDto patientProfileDto = (PatientProfileWriteDto) userProfileUpdateDto;
        setPatientProfileData(patientProfile, patientProfileDto);
        userProfileRepository.save(patientProfile);
    }
    
    @Override
    public UserProfileReadDto findByUserId(Long userId) {
        final var userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found for user ID: " + userId));
        final var patientProfile = (PatientProfile) userProfile;
        final var userDto = new UserReadDto(userId, patientProfile.getUser().getActualUsername(),
                patientProfile.getUser().getEmail(), patientProfile.getUser().getRole());

        return getPatientProfileReadDto(patientProfile, userDto);
    }

    private void setPatientProfileData(PatientProfile newPatientProfile, PatientProfileWriteDto patientProfileDto) {
        newPatientProfile.setPhoneNumber(patientProfileDto.getPhoneNumber());
        newPatientProfile.setGender(patientProfileDto.getGender());
        chooseSymptoms(newPatientProfile, patientProfileDto.getSymptomIds());
    }

    private void chooseSymptoms(PatientProfile patientProfile, List<Long> symptomIds) {
        List<Symptom> symptoms = symptomRepository.findAllById(symptomIds);
        if (symptoms.size() != symptomIds.size()) {
            throw new IllegalArgumentException("One or more symptoms not found.");
        }
        patientProfile.setSymptoms(symptoms);
    }


    private void authorizeUserAction(Long targetUserId, User authenticatedUser) {
        if (!targetUserId.equals(authenticatedUser.getId()) || !authenticatedUser.getRole().equals(Role.PATIENT)) {
            throw new UnauthorizedException("User with id " + authenticatedUser.getId() + " not authorized.");
        }
    }

    private UserProfileReadDto getPatientProfileReadDto(PatientProfile patientProfile, UserReadDto userDto) {
            return new PatientProfileReadDto(userDto, patientProfile.getId(), patientProfile.getPhoneNumber(), patientProfile.getGender(),
                    patientProfile.getAnxietyRecords().stream().map((element) -> mapper.map(element, AnxietyRecordReadDto.class)).collect(Collectors.toList()),
                    patientProfile.getDisorders().stream().map((element) -> mapper.map(element, DisorderDto.class)).collect(Collectors.toList()),
                    patientProfile.getSymptoms().stream().map((element) -> mapper.map(element, SymptomDto.class)).collect(Collectors.toList()));
        }
}
