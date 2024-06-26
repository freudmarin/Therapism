package com.marindulja.mentalhealthbackend.models;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    PATIENT,
    THERAPIST,
    ADMIN,

    SUPERADMIN;

    public static Role fromString(String role) {
        return Role.valueOf(role.toUpperCase());
    }

    @Override
    public String getAuthority() {
        return name();
    }
}
