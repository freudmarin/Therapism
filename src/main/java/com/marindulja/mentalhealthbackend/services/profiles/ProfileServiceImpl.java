package com.marindulja.mentalhealthbackend.services.profiles;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.UserProfileDto;
import com.marindulja.mentalhealthbackend.exceptions.UnauthorizedException;
import com.marindulja.mentalhealthbackend.models.Gender;
import com.marindulja.mentalhealthbackend.models.User;
import com.marindulja.mentalhealthbackend.models.UserProfile;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository userProfileRepository;

    private final ModelMapper mapper = new ModelMapper();
    public ProfileServiceImpl(ProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public UserProfileDto createProfile(Long userId, UserProfileDto userProfileDto) {
        User currentUser = Utilities.getCurrentUser().get();

        if(!userId.equals(currentUser.getId())) {
            throw new UnauthorizedException("User with id " + currentUser.getId() + " not authorized to update user with id " + userId);
        }
        UserProfile newUserProfile = mapToEntity(userProfileDto);
        newUserProfile.setUser(currentUser);
        return mapToDTO(userProfileRepository.save(newUserProfile));
    }

    @Override
    public UserProfileDto updateProfile(Long userId, UserProfileDto userProfileDto) {

        User currentUser = Utilities.getCurrentUser().get();

        if(!userId.equals(currentUser.getId())) {
            throw new UnauthorizedException("User with id " + currentUser.getId() + " not authorized to update user with id " + userId);
        }

        UserProfile existingProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found for user ID: " + userId));


        existingProfile.setPhoneNumber(userProfileDto.getPhoneNumber());
        existingProfile.setGender(Gender.valueOf(userProfileDto.getGender()));
        UserProfile userProfile = userProfileRepository.save(existingProfile);
        return mapToDTO(userProfile);
    }

    @Override
    public UserProfileDto findByUserId(Long userId) {
        return mapToDTO(userProfileRepository.findByUserId(userId).orElseThrow(() -> new EntityNotFoundException("Profile not found for user ID: " + userId)));
    }
    private UserProfileDto mapToDTO(UserProfile userProfile) {
        return mapper.map(userProfile, UserProfileDto.class);
    }

    private UserProfile mapToEntity(UserProfileDto userProfileDto) {
        return mapper.map(userProfileDto, UserProfile.class);
    }
}
