package com.marindulja.mentalhealthbackend.services.profiles;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.*;
import com.marindulja.mentalhealthbackend.exceptions.UnauthorizedException;
import com.marindulja.mentalhealthbackend.models.User;
import com.marindulja.mentalhealthbackend.models.UserProfile;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import com.marindulja.mentalhealthbackend.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository userProfileRepository;

    private final ModelMapper mapper = new ModelMapper();
    private final UserRepository userRepository;

    public ProfileServiceImpl(ProfileRepository userProfileRepository,
                              UserRepository userRepository) {
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
    }

    @Override
    public UserProfileDto createProfile(Long userId, UserProfileCreationOrUpdateDto userProfileCreationDto) {
        User cUser = Utilities.getCurrentUser().get();

        if(!userId.equals(cUser.getId())) {
            throw new UnauthorizedException("User with id " + cUser.getId() + " not authorized to create profile for user with id " + userId);
        }

        User currentUser = userRepository.findById(cUser.getId())
                .orElseThrow(() -> new UnauthorizedException("No authenticated user found."));


        UserProfile newUserProfile = mapUserProfileCreationDtoToEntity(userProfileCreationDto);
        newUserProfile.setUser(currentUser);
        // Save the UserProfile
        UserProfile savedUserProfile = userProfileRepository.save(newUserProfile);
        // Map the saved UserProfile to DTO and return
        return mapToDTO(savedUserProfile);
    }

    @Override
    public UserProfileDto updateProfile(Long userId, UserProfileCreationOrUpdateDto userProfileCreationOrUpdateDto) {

        User currentUser = Utilities.getCurrentUser().get();

        if(!userId.equals(currentUser.getId())) {
            throw new UnauthorizedException("User with id " + currentUser.getId() + " not authorized to update user with id " + userId);
        }

        UserProfile existingProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found for user ID: " + userId));
        existingProfile.setPhoneNumber(userProfileCreationOrUpdateDto.getPhoneNumber());
        existingProfile.setGender(userProfileCreationOrUpdateDto.getGender());
        UserProfile userProfile = userProfileRepository.save(existingProfile);
        return mapToDTO(userProfile);
    }

    @Override
    public UserProfileWithUserDto findByUserId(Long userId) {

        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found for user ID: " + userId));

        UserDto userDto = mapToUserDTO(userProfile.getUser());

        UserProfileWithUserDto userProfileWithUserDto = new UserProfileWithUserDto();
        userProfileWithUserDto.setProfileId(userProfile.getId());
        userProfileWithUserDto.setPhoneNumber(userProfile.getPhoneNumber());
        userProfileWithUserDto.setGender(userProfile.getGender());
        userProfileWithUserDto.setUserDto(userDto);
        userProfileWithUserDto.setDisorders(userProfile.getDisorders().stream().map((element) -> mapper.map(element, DisorderDto.class)).collect(Collectors.toList()));
        userProfileWithUserDto.setAnxietyRecords(userProfile.getAnxietyRecords().stream().map((element) -> mapper.map(element, AnxietyRecordDto.class)).collect(Collectors.toList()));
        return userProfileWithUserDto;
    }
    private UserProfileDto mapToDTO(UserProfile userProfile) {
        return mapper.map(userProfile, UserProfileDto.class);
    }

    private UserProfile mapToEntity(UserProfileWithUserDto userProfileDto) {
        return mapper.map(userProfileDto, UserProfile.class);
    }
    private UserProfile mapUserProfileCreationDtoToEntity(UserProfileCreationOrUpdateDto userProfileDto) {
        return mapper.map(userProfileDto, UserProfile.class);
    }

    private UserDto mapToUserDTO(User user) {
        return mapper.map(user, UserDto.class);
    }
}
