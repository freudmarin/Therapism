package com.marindulja.mentalhealthbackend.services.profiles;

import com.marindulja.mentalhealthbackend.dtos.UserProfileDto;

public interface ProfileService {
    UserProfileDto updateProfile(Long userId, UserProfileDto userProfileDto);

    UserProfileDto findByUserId(Long userId);

}
