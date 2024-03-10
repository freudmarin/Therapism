package com.marindulja.mentalhealthbackend.services.profiles;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.*;
import com.marindulja.mentalhealthbackend.dtos.mapping.ModelMappingUtility;
import com.marindulja.mentalhealthbackend.exceptions.UnauthorizedException;
import com.marindulja.mentalhealthbackend.models.UserProfile;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import com.marindulja.mentalhealthbackend.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository userProfileRepository;

    private final ModelMappingUtility mapper;
    private final UserRepository userRepository;

    public ProfileServiceImpl(ProfileRepository userProfileRepository, ModelMappingUtility mapper,
                              UserRepository userRepository) {
        this.userProfileRepository = userProfileRepository;
        this.mapper = mapper;
        this.userRepository = userRepository;
    }

    @Override
    public UserProfileReadDto createProfile(Long userId, UserProfileWriteDto userProfileCreationDto) {
        final var cUser = Utilities.getCurrentUser().get();

        if(!userId.equals(cUser.getId())) {
            throw new UnauthorizedException("User with id " + cUser.getId() + " not authorized to create profile for user with id " + userId);
        }

        final var currentUser = userRepository.findById(cUser.getId())
                .orElseThrow(() -> new UnauthorizedException("No authenticated user found."));


        final var newUserProfile = mapper.map(userProfileCreationDto, UserProfile.class);
        newUserProfile.setUser(currentUser);
        // Save the UserProfile
        final var savedUserProfile = userProfileRepository.save(newUserProfile);
        // Map the saved UserProfile to DTO and return
        return mapper.map(savedUserProfile, UserProfileReadDto.class);
    }

    @Override
    public UserProfileReadDto updateProfile(Long userId, UserProfileWriteDto userProfileCreationOrUpdateDto) {

        final var currentUser = Utilities.getCurrentUser().get();

        if(!userId.equals(currentUser.getId())) {
            throw new UnauthorizedException("User with id " + currentUser.getId() + " not authorized to update user with id " + userId);
        }

        final var existingProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found for user ID: " + userId));
        existingProfile.setPhoneNumber(userProfileCreationOrUpdateDto.getPhoneNumber());
        existingProfile.setGender(userProfileCreationOrUpdateDto.getGender());
        UserProfile userProfile = userProfileRepository.save(existingProfile);
        return mapper.map(userProfile, UserProfileReadDto.class);
    }

    @Override
    public UserProfileWithUserDto findByUserId(Long userId) {

        final var userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found for user ID: " + userId));

        final var userDto = mapper.map(userProfile.getUser(), UserReadDto.class);

        UserProfileWithUserDto userProfileWithUserDto = new UserProfileWithUserDto();
        userProfileWithUserDto.setProfileId(userProfile.getId());
        userProfileWithUserDto.setPhoneNumber(userProfile.getPhoneNumber());
        userProfileWithUserDto.setGender(userProfile.getGender());
        userProfileWithUserDto.setUserDto(userDto);
        userProfileWithUserDto.setDisorders(userProfile.getDisorders().stream().map((element) -> mapper.map(element, DisorderDto.class)).collect(Collectors.toList()));
        userProfileWithUserDto.setAnxietyRecords(userProfile.getAnxietyRecords().stream().map((element) -> mapper.map(element, AnxietyRecordDto.class)).collect(Collectors.toList()));
        return userProfileWithUserDto;
    }
}
