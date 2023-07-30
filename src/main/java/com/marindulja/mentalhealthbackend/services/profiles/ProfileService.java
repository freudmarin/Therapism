package com.marindulja.mentalhealthbackend.services.profiles;

import com.marindulja.mentalhealthbackend.dtos.UserProfileDto;
import com.marindulja.mentalhealthbackend.models.UserProfile;

public interface ProfileService {
    UserProfile updateProfile(Long userId, UserProfileDto userProfileDto);

    UserProfile findByUserId(Long userId);

}
