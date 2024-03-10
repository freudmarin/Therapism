package com.marindulja.mentalhealthbackend.services.profiles;

import com.marindulja.mentalhealthbackend.dtos.UserProfileReadDto;
import com.marindulja.mentalhealthbackend.dtos.UserProfileWithUserDto;
import com.marindulja.mentalhealthbackend.dtos.UserProfileWriteDto;

public interface ProfileService {

    UserProfileReadDto createProfile(Long userId, UserProfileWriteDto userProfileCreationDto);
    UserProfileReadDto updateProfile(Long userId, UserProfileWriteDto userProfileCreationOrUpdateDto);

    UserProfileWithUserDto findByUserId(Long userId);

}
