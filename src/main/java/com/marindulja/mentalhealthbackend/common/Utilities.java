package com.marindulja.mentalhealthbackend.common;

import com.marindulja.mentalhealthbackend.models.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
    public class Utilities {

        public static Optional<User> getCurrentUser() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getPrincipal() instanceof User) {
                return Optional.of((User) authentication.getPrincipal());
            }

            return Optional.empty();
        }
}
