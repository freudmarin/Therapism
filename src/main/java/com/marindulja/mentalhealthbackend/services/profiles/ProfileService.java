package com.marindulja.mentalhealthbackend.services.profiles;

import com.marindulja.mentalhealthbackend.dtos.UserProfileCreationOrUpdateDto;
import com.marindulja.mentalhealthbackend.dtos.UserProfileDto;
import com.marindulja.mentalhealthbackend.dtos.UserProfileWithUserDto;

public interface ProfileService {

    UserProfileDto createProfile(Long userId, UserProfileCreationOrUpdateDto userProfileCreationDto);
    UserProfileDto updateProfile(Long userId, UserProfileCreationOrUpdateDto userProfileCreationOrUpdateDto);

    UserProfileWithUserDto findByUserId(Long userId);

}
