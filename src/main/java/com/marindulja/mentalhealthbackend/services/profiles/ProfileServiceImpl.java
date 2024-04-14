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
import jakarta.transaction.Transactional;
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
    @Transactional
    public UserProfileReadDto createProfile(Long userId, UserProfileWriteDto userProfileCreationDto) {
        var currentUser = getCurrentAuthenticatedUser();
        authorizeUserAction(userId, currentUser.getId());

        // Since we're within a transaction, this should ensure currentUser is managed
        currentUser = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new UnauthorizedException("No authenticated user found."));

        final var newUserProfile = determineProfileTypeByRole(currentUser, userProfileCreationDto);
        newUserProfile.setUser(currentUser);

        final var savedUserProfile = userProfileRepository.save(newUserProfile);
        return getUserProfileReadDto(savedUserProfile, mapper.map(savedUserProfile.getUser(), UserReadDto.class));
    }

    @Override
    @Transactional
    public UserProfileReadDto updateProfile(Long userId, UserProfileWriteDto userProfileCreationOrUpdateDto) {
        final var currentUser = getCurrentAuthenticatedUser();
        authorizeUserAction(userId, currentUser.getId());
        final var existingProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found for user ID: " + userId));
        existingProfile.setPhoneNumber(userProfileCreationOrUpdateDto.getPhoneNumber());
        existingProfile.setGender(userProfileCreationOrUpdateDto.getGender());
        final var userProfile = userProfileRepository.save(existingProfile);
        final var userDto = mapper.map(userProfile.getUser(), UserReadDto.class);

        return getUserProfileReadDto(userProfile, userDto);
    }

    @Override
    public UserProfileReadDto findByUserId(Long userId) {
        final var userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found for user ID: " + userId));

        final var userDto = new UserReadDto(userId, userProfile.getUser().getActualUsername(),
                userProfile.getUser().getEmail(), userProfile.getUser().getRole());

        return getUserProfileReadDto(userProfile, userDto);
    }

    @Override
    public void updateTherapistProfile(Long therapistId, TherapistProfileUpdateDto therapistProfileUpdateDto) {
        final var currentUser = getCurrentAuthenticatedUser();
        authorizeUserAction(therapistId, currentUser.getId());

        final var therapistProfile = userProfileRepository.findByUserId(therapistId)
                .filter(TherapistProfile.class::isInstance)
                .map(TherapistProfile.class::cast)
                .orElseThrow(() -> new EntityNotFoundException("Therapist profile not found for ID: " + therapistId));

        updateTherapistSpecializations(therapistProfile, therapistProfileUpdateDto.getSpecializationIds());
        therapistProfile.setQualifications(therapistProfileUpdateDto.getQualifications());
        therapistProfile.setYearsOfExperience(therapistProfileUpdateDto.getYearsOfExperience());

        userProfileRepository.save(therapistProfile);
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

    private UserProfile determineProfileTypeByRole(User user, UserProfileWriteDto dto) {
        return switch (user.getRole()) {
            case ADMIN -> mapper.map(dto, AdminProfile.class);
            case PATIENT -> mapper.map(dto, PatientProfile.class);
            case THERAPIST -> mapper.map(dto, TherapistProfile.class);
            default -> mapper.map(dto, SuperAdminProfile.class);
        };
    }

    private void updateTherapistSpecializations(TherapistProfile therapistProfile, List<Long> specializationIds) {
        final var specializations = specializationRepository.findAllById(specializationIds);
        therapistProfile.setSpecializations(specializations);
    }

    public UserProfileReadDto getUserProfileReadDto(UserProfile userProfile, UserReadDto userDto) {
        if (userProfile instanceof PatientProfile patientProfile) {
            final var patientProfileReadDto = new PatientProfileReadDto();
            patientProfileReadDto.setProfileId(patientProfile.getId());
            patientProfileReadDto.setPhoneNumber(patientProfile.getPhoneNumber());
            patientProfileReadDto.setGender(patientProfile.getGender());
            patientProfileReadDto.setUserDto(userDto);
            patientProfileReadDto.setDisorders(patientProfile.getDisorders().stream().map((element) -> mapper.map(element, DisorderDto.class)).collect(Collectors.toList()));
            patientProfileReadDto.setAnxietyRecords(patientProfile.getAnxietyRecords().stream().map((element) -> mapper.map(element, AnxietyRecordReadDto.class)).collect(Collectors.toList()));
            return patientProfileReadDto;
        }
        if (userProfile instanceof TherapistProfile therapistProfile) {
            final var therapistProfileReadDto = new TherapistProfileReadDto();
            therapistProfileReadDto.setProfileId(therapistProfile.getId());
            therapistProfileReadDto.setPhoneNumber(therapistProfile.getPhoneNumber());
            therapistProfileReadDto.setGender(therapistProfile.getGender());
            therapistProfileReadDto.setUserDto(userDto);
            therapistProfileReadDto.setQualifications(therapistProfile.getQualifications());
            therapistProfileReadDto.setYearsOfExperience(therapistProfile.getYearsOfExperience());
            therapistProfileReadDto.setSpecializations(therapistProfile.getSpecializations());
            return therapistProfileReadDto;
        } else {
            final var userProfileReadDto = new UserProfileReadDto();
            userProfileReadDto.setProfileId(userProfile.getId());
            userProfileReadDto.setPhoneNumber(userProfile.getPhoneNumber());
            userProfileReadDto.setGender(userProfile.getGender());
            userProfileReadDto.setUserDto(userDto);
            return userProfileReadDto;
        }
    }
}
