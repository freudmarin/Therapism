package com.marindulja.mentalhealthbackend.services.profiles;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.*;
import com.marindulja.mentalhealthbackend.dtos.mapping.ModelMappingUtility;
import com.marindulja.mentalhealthbackend.exceptions.UnauthorizedException;
import com.marindulja.mentalhealthbackend.models.*;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import com.marindulja.mentalhealthbackend.repositories.SpecializationRepository;
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
        newUserProfile.setPhoneNumber(userProfileCreationDto.getPhoneNumber());
        newUserProfile.setGender(userProfileCreationDto.getGender());
        final var savedUserProfile = userProfileRepository.save(newUserProfile);
        return getUserProfileReadDto(savedUserProfile, mapper.map(savedUserProfile.getUser(), UserReadDto.class));
    }

    @Override
    public void updateProfile(Long userId, UserProfileWriteDto userProfileUpdateDto) {
        var currentUser = getCurrentAuthenticatedUser();
        authorizeUserAction(userId, currentUser.getId());

        final var userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found for user ID: " + userId));
        userProfile.setPhoneNumber(userProfileUpdateDto.getPhoneNumber());
        userProfile.setGender(userProfileUpdateDto.getGender());
        userProfileRepository.save(userProfile);
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
        therapistProfile.setPhoneNumber(therapistProfileUpdateDto.getPhoneNumber());
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
        if (specializations.size() != specializationIds.size()) {
            throw new IllegalArgumentException("One or more specializations not found.");
        }
        therapistProfile.setSpecializations(specializations);
    }

    public UserProfileReadDto getUserProfileReadDto(UserProfile userProfile, UserReadDto userDto) {
        if (userProfile instanceof PatientProfile patientProfile) {
            return new PatientProfileReadDto(userDto, patientProfile.getId(), patientProfile.getPhoneNumber(), patientProfile.getGender(),
                    patientProfile.getAnxietyRecords().stream().map((element) -> mapper.map(element, AnxietyRecordReadDto.class)).collect(Collectors.toList()),
                    patientProfile.getDisorders().stream().map((element) -> mapper.map(element, DisorderDto.class)).collect(Collectors.toList()));
        }
        if (userProfile instanceof TherapistProfile therapistProfile) {
            return new TherapistProfileReadDto(
                    userDto,
                    therapistProfile.getId(),
                    therapistProfile.getPhoneNumber(),
                    therapistProfile.getGender(),
                    therapistProfile.getYearsOfExperience(),
                    therapistProfile.getQualifications(),
                    therapistProfile.getSpecializations());
        } else {
            return new UserProfileReadDto(userDto, userProfile.getId(),
                    userProfile.getPhoneNumber(), userProfile.getGender());
        }
    }
}
