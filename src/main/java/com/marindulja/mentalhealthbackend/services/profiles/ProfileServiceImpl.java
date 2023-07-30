package com.marindulja.mentalhealthbackend.services.profiles;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.UserProfileDto;
import com.marindulja.mentalhealthbackend.exceptions.UnauthorizedException;
import com.marindulja.mentalhealthbackend.models.User;
import com.marindulja.mentalhealthbackend.models.UserProfile;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository userProfileRepository;

    public ProfileServiceImpl(ProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public UserProfile updateProfile(Long userId, UserProfileDto userProfileDto) {

        User currentUser = Utilities.getCurrentUser().get();

        if(!userId.equals(currentUser.getId())) {
            throw new UnauthorizedException("User with id " + currentUser.getId() + " not authorized to update user with id " + userId);
        }

        UserProfile existingProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found for user ID: " + userId));

        String[] nameParts = userProfileDto.getFullName().split(" ");

        if (nameParts.length > 0) {
            existingProfile.setName(nameParts[0]);
        }
        if (nameParts.length > 1) {
            existingProfile.setSurname(nameParts[1]);
        }

        existingProfile.setPhoneNumber(userProfileDto.getPhoneNumber());

        return userProfileRepository.save(existingProfile);
    }

    @Override
    public UserProfile findByUserId(Long userId) {
        return userProfileRepository.findByUserId(userId).orElseThrow(() -> new EntityNotFoundException("Profile not found for user ID: " + userId));
    }

}
