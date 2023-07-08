package com.marindulja.mentalhealthbackend.services.auth;

import com.marindulja.mentalhealthbackend.adapters.UserAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    public Optional<UserAdapter> getCurrentUser() {
        UserAdapter principal = (UserAdapter) SecurityContextHolder.
                getContext().getAuthentication().getPrincipal();
        return Optional.of(principal);
    }
}
