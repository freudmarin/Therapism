package com.marindulja.mentalhealthbackend.services.profiles;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.*;
import com.marindulja.mentalhealthbackend.dtos.mapping.ModelMappingUtility;
import com.marindulja.mentalhealthbackend.exceptions.UnauthorizedException;
import com.marindulja.mentalhealthbackend.models.*;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import com.marindulja.mentalhealthbackend.repositories.SpecializationRepository;
import com.marindulja.mentalhealthbackend.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository userProfileRepository;

    private final ModelMappingUtility mapper;
    private final UserRepository userRepository;

    private final SpecializationRepository specializationRepository;

    @Override
    public UserProfileReadDto createProfile(Long userId, UserProfileWriteDto userProfileCreationDto) {
        final var cUser = Utilities.getCurrentUser().get();

        if (!userId.equals(cUser.getId())) {
            throw new UnauthorizedException("User with id " + cUser.getId() + " not authorized to create profile for user with id " + userId);
        }

        final var currentUser = userRepository.findById(cUser.getId())
                .orElseThrow(() -> new UnauthorizedException("No authenticated user found."));

        UserProfile newUserProfile;
        if (currentUser.getRole() == Role.ADMIN) {
            newUserProfile = mapper.map(userProfileCreationDto, AdminProfile.class);
        } else if (currentUser.getRole() == Role.PATIENT) {
            newUserProfile = mapper.map(userProfileCreationDto, PatientProfile.class);
        } else if (currentUser.getRole() == Role.THERAPIST) {
            newUserProfile = mapper.map(userProfileCreationDto, TherapistProfile.class);
        } else {
            newUserProfile = mapper.map(userProfileCreationDto, SuperAdminProfile.class);
        }
        newUserProfile.setUser(currentUser);
        // Save the UserProfile
        final var savedUserProfile = userProfileRepository.save(newUserProfile);
        // Map the saved UserProfile to DTO and return
        final var userDto = mapper.map(savedUserProfile.getUser(), UserReadDto.class);

        return getUserProfileReadDto(savedUserProfile, userDto);
    }

    @Override
    public UserProfileReadDto updateProfile(Long userId, UserProfileWriteDto userProfileCreationOrUpdateDto) {

        final var currentUser = Utilities.getCurrentUser().get();

        if (!userId.equals(currentUser.getId())) {
            throw new UnauthorizedException("User with id " + currentUser.getId() + " not authorized to update user with id " + userId);
        }

        final var existingProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found for user ID: " + userId));
        existingProfile.setPhoneNumber(userProfileCreationOrUpdateDto.getPhoneNumber());
        existingProfile.setGender(userProfileCreationOrUpdateDto.getGender());
        UserProfile userProfile = userProfileRepository.save(existingProfile);
        final var userDto = mapper.map(userProfile.getUser(), UserReadDto.class);

        return getUserProfileReadDto(userProfile, userDto);
    }

    @Override
    public UserProfileReadDto findByUserId(Long userId) {

        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found for user ID: " + userId));

        final var userDto = mapper.map(userProfile.getUser(), UserReadDto.class);

        return getUserProfileReadDto(userProfile, userDto);
    }

    @Override
    public void updateTherapistProfile(Long therapistId, TherapistProfileUpdateDto therapistProfileCompletionDto) {
        final var currentUser = Utilities.getCurrentUser().get();

        if (!therapistId.equals(currentUser.getId())) {
            throw new UnauthorizedException("User with id " + currentUser.getId() + " not authorized to update profile with id " + therapistId);
        }

        final var userProfile = userProfileRepository.findByUserId(therapistId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found for therapist with ID: " + therapistId));
        List<Specialization> therapistSpecializationList = specializationRepository
                .findAllById(therapistProfileCompletionDto.getSpecializationIds());

        if (userProfile instanceof TherapistProfile therapistProfile ) {
            therapistProfile.setSpecializations(therapistSpecializationList);
            therapistProfile.setQualifications(therapistProfileCompletionDto.getQualifications());
            therapistProfile.setYearsOfExperience(therapistProfileCompletionDto.getYearsOfExperience());
            userProfileRepository.save(userProfile);
        }
    }

    public UserProfileReadDto getUserProfileReadDto(UserProfile userProfile, UserReadDto userDto) {
        if (userProfile instanceof PatientProfile patientProfile) {
            PatientProfileReadDto patientProfileReadDto = new PatientProfileReadDto();
            patientProfileReadDto.setProfileId(patientProfile.getId());
            patientProfileReadDto.setPhoneNumber(patientProfile.getPhoneNumber());
            patientProfileReadDto.setGender(patientProfile.getGender());
            patientProfileReadDto.setUserDto(userDto);
            patientProfileReadDto.setDisorders(patientProfile.getDisorders().stream().map((element) -> mapper.map(element, DisorderDto.class)).collect(Collectors.toList()));
            patientProfileReadDto.setAnxietyRecords(patientProfile.getAnxietyRecords().stream().map((element) -> mapper.map(element, AnxietyRecordReadDto.class)).collect(Collectors.toList()));
            return patientProfileReadDto;
        }
        if (userProfile instanceof TherapistProfile therapistProfile) {
            TherapistProfileReadDto therapistProfileReadDto = new TherapistProfileReadDto();
            therapistProfileReadDto.setProfileId(therapistProfile.getId());
            therapistProfileReadDto.setPhoneNumber(therapistProfile.getPhoneNumber());
            therapistProfileReadDto.setGender(therapistProfile.getGender());
            therapistProfileReadDto.setUserDto(userDto);
            therapistProfileReadDto.setQualifications(therapistProfile.getQualifications());
            therapistProfileReadDto.setYearsOfExperience(therapistProfile.getYearsOfExperience());
            therapistProfileReadDto.setSpecializations(therapistProfile.getSpecializations());
            return therapistProfileReadDto;
        } else {
            UserProfileReadDto userProfileReadDto = new UserProfileReadDto();
            userProfileReadDto.setProfileId(userProfile.getId());
            userProfileReadDto.setPhoneNumber(userProfile.getPhoneNumber());
            userProfileReadDto.setGender(userProfile.getGender());
            userProfileReadDto.setUserDto(userDto);
            return userProfileReadDto;
        }
    }
}
