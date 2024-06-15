package com.marindulja.mentalhealthbackend.services.profiles;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.*;
import com.marindulja.mentalhealthbackend.dtos.mapping.ModelMappingUtility;
import com.marindulja.mentalhealthbackend.exceptions.UnauthorizedException;
import com.marindulja.mentalhealthbackend.models.Role;
import com.marindulja.mentalhealthbackend.models.TherapistProfile;
import com.marindulja.mentalhealthbackend.models.User;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import com.marindulja.mentalhealthbackend.repositories.SpecializationRepository;
import com.marindulja.mentalhealthbackend.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TherapistProfileServiceImpl implements ProfileService {

    private final ProfileRepository userProfileRepository;

    private final ModelMappingUtility mapper;

    private final SpecializationRepository specializationRepository;

    private final UserRepository userRepository;
    private final ModelMappingUtility modelMapper;

    @Override
    @Transactional
    public UserProfileReadDto createProfile(Long userId, UserProfileWriteDto profileDto) {
        User currentUser = Utilities.getCurrentUser().flatMap(cUser -> userRepository.findById(cUser.getId()))
                .orElseThrow(() -> new UnauthorizedException("No authenticated user found"));
        authorizeUserAction(userId, currentUser);
        TherapistProfileWriteDto therapistProfileDto = (TherapistProfileWriteDto) profileDto;
        TherapistProfile therapistProfile = mapper.map(therapistProfileDto, TherapistProfile.class);
        therapistProfile.setUser(currentUser);
        setTherapistProfileData(therapistProfile, therapistProfileDto);
        var savedTherapistProfile = userProfileRepository.save(therapistProfile);
        return getTherapistProfileReadDto(savedTherapistProfile, mapper.map(savedTherapistProfile.getUser(), UserReadDto.class));
    }

    @Override
    public void updateProfile(Long userId, UserProfileWriteDto profileDto) {
        User currentUser = Utilities.getCurrentUser().flatMap(cUser -> userRepository.findById(cUser.getId()))
                .orElseThrow(() -> new UnauthorizedException("No authenticated user found"));
        authorizeUserAction(userId, currentUser);

        TherapistProfile therapistProfile = userProfileRepository.findByUserId(userId)
                .filter(TherapistProfile.class::isInstance)
                .map(TherapistProfile.class::cast)
                .orElseThrow(() -> new EntityNotFoundException("Therapist profile not found for user ID: " + userId));
        TherapistProfileWriteDto therapistProfileDto = (TherapistProfileWriteDto) profileDto;
        setTherapistProfileData(therapistProfile, therapistProfileDto);
        userProfileRepository.save(therapistProfile);
    }

    private void setTherapistProfileData(TherapistProfile therapistProfile, TherapistProfileWriteDto therapistProfileDto) {
        therapistProfile.setPhoneNumber(therapistProfileDto.getPhoneNumber());
        therapistProfile.setGender(therapistProfileDto.getGender());
        updateTherapistSpecializations(therapistProfile, therapistProfileDto.getSpecializationIds());
        therapistProfile.setQualifications(therapistProfileDto.getQualifications());
        therapistProfile.setYearsOfExperience(therapistProfileDto.getYearsOfExperience());
    }

    @Override
    public UserProfileReadDto findByUserId(Long userId) {
        final var userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found for user ID: " + userId));
        final var therapistProfile = (TherapistProfile) userProfile;
        final var userDto = new UserReadDto(userId, therapistProfile.getUser().getActualUsername(),
                therapistProfile.getUser().getEmail(), therapistProfile.getUser().getRole());

        return getTherapistProfileReadDto(therapistProfile, userDto);
    }

    private void authorizeUserAction(Long targetUserId, User authenticatedUser) {
        if (!targetUserId.equals(authenticatedUser.getId()) || !authenticatedUser.getRole().equals(Role.THERAPIST)) {
            throw new UnauthorizedException("User with id " + authenticatedUser.getId() + " not authorized.");
        }
    }

    private void updateTherapistSpecializations(TherapistProfile therapistProfile, List<Long> specializationIds) {
        final var specializations = specializationRepository.findAllById(specializationIds);
        if (specializations.size() != specializationIds.size()) {
            throw new IllegalArgumentException("One or more specializations not found.");
        }
        therapistProfile.setSpecializations(specializations);
    }

    public TherapistProfileReadDto getTherapistProfileReadDto(TherapistProfile therapistProfile, UserReadDto userDto) {
        return new TherapistProfileReadDto(
                userDto,
                therapistProfile.getId(),
                therapistProfile.getPhoneNumber(),
                therapistProfile.getGender(),
                therapistProfile.getYearsOfExperience(),
                therapistProfile.getQualifications(),
                therapistProfile.getSpecializations().stream().map((element) -> modelMapper.map(element, SpecializationDto.class)).toList());
    }
}
