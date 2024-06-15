package com.marindulja.mentalhealthbackend.services.profiles;

import com.marindulja.mentalhealthbackend.dtos.UserProfileReadDto;
import com.marindulja.mentalhealthbackend.dtos.UserProfileWriteDto;

public interface ProfileService {
    UserProfileReadDto createProfile(Long userId, UserProfileWriteDto userProfileCreationDto);
    void updateProfile(Long userId, UserProfileWriteDto userProfileUpdateDto);
    UserProfileReadDto findByUserId(Long userId);
}
