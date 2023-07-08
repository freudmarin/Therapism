package com.marindulja.mentalhealthbackend.models;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    PATIENT,
    THERAPIST,
    ADMIN,

    SUPERADMIN;

    @Override
    public String getAuthority() {
        return name();
    }
}
