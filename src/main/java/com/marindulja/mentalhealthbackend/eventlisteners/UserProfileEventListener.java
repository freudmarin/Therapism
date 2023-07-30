package com.marindulja.mentalhealthbackend.eventlisteners;

import com.marindulja.mentalhealthbackend.models.User;
import com.marindulja.mentalhealthbackend.models.UserProfile;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserProfileEventListener {

    private final ProfileRepository userProfileRepository;


    @EventListener
    public void handleUserCreate(UserCreatedEvent event) {
        User user = event.getUser();
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        userProfileRepository.save(profile);
    }
}
