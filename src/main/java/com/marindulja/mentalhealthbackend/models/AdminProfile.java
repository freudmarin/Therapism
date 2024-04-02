package com.marindulja.mentalhealthbackend.models;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("Admin")
@Getter
@Setter
@NoArgsConstructor
public class AdminProfile extends UserProfile {

    public AdminProfile(Long id, String phoneNumber, Gender gender, User user) {
        super(id, phoneNumber, gender, false, user);
    }
}
