package com.marindulja.mentalhealthbackend.services.profiles;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.mapping.DTOMappings;
import com.marindulja.mentalhealthbackend.dtos.profile.PatientProfileReadDto;
import com.marindulja.mentalhealthbackend.dtos.profile.PatientProfileWriteDto;
import com.marindulja.mentalhealthbackend.dtos.profile.UserProfileReadDto;
import com.marindulja.mentalhealthbackend.dtos.profile.UserProfileWriteDto;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientProfileServiceImpl implements ProfileService {

    private final DTOMappings mapper;
    private final SymptomRepository symptomRepository;
    private final ProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    @Override
    public UserProfileReadDto createProfile(Long userId, UserProfileWriteDto userProfileCreationDto) {
        User user = Utilities.getCurrentUser().flatMap(currentUser -> userRepository.findById(currentUser.getId()))
                .orElseThrow(() -> new UnauthorizedException("No authenticated user found"));
        authorizeUserAction(userId, user);
        PatientProfileWriteDto patientProfileDto = (PatientProfileWriteDto) userProfileCreationDto;
        PatientProfile newPatientProfile = mapper.toPatientProfile(patientProfileDto);
        newPatientProfile.setUser(user);
        setPatientProfileData(newPatientProfile, patientProfileDto);
        var savedPatientProfile = userProfileRepository.save(newPatientProfile);
        return getPatientProfileReadDto(savedPatientProfile, mapper.toUserDTO(savedPatientProfile.getUser()));
    }

    @Override
    public void updateProfile(Long patientId, UserProfileWriteDto userProfileUpdateDto) {
        User user = Utilities.getCurrentUser().flatMap(currentUser -> userRepository.findById(currentUser.getId()))
                .orElseThrow(() -> new UnauthorizedException("No authenticated user found"));
        authorizeUserAction(patientId, user);
        PatientProfile patientProfile = userProfileRepository.findByUserId(patientId)
                .filter(PatientProfile.class::isInstance)
                .map(PatientProfile.class::cast)
                .orElseThrow(() -> new EntityNotFoundException("Patient profile not found for patient ID: " + patientId));
        PatientProfileWriteDto patientProfileDto = (PatientProfileWriteDto) userProfileUpdateDto;
        setPatientProfileData(patientProfile, patientProfileDto);
        userProfileRepository.save(patientProfile);
    }
    
    @Override
    public UserProfileReadDto findByUserId(Long userId) {
        final var userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found for user ID: " + userId));
        if (userProfile.getUser().getRole() != Role.PATIENT) {
            throw new UnauthorizedException("User with id " + userId + " is not a patient.");
        }
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
        if (!targetUserId.equals(authenticatedUser.getId()) || (!authenticatedUser.getRole().equals(Role.PATIENT) &&
                !authenticatedUser.getRole().equals(Role.ADMIN))) {
            throw new UnauthorizedException("User with id " + authenticatedUser.getId() + " not authorized.");
        }
    }

    private UserProfileReadDto getPatientProfileReadDto(PatientProfile patientProfile, UserReadDto userDto) {
            return new PatientProfileReadDto(userDto, patientProfile.getId(), patientProfile.getPhoneNumber(), patientProfile.getGender(),
                    patientProfile.getAnxietyRecords().stream().map(mapper::toAnxietyRecordReadDto).toList(),
                    patientProfile.getDisorders().stream().map(mapper::toDisorderDto).toList(),
                    patientProfile.getSymptoms().stream().map(mapper::toSymptomDto).toList());
        }
}
