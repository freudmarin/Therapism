package com.marindulja.mentalhealthbackend.eventlisteners;

import com.marindulja.mentalhealthbackend.models.User;
import org.springframework.context.ApplicationEvent;

public class UserCreatedEvent extends ApplicationEvent {
    private final User user;

    public UserCreatedEvent(User user) {
        super(user);
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
