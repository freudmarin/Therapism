package com.marindulja.mentalhealthbackend.models;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("SuperAdmin")
@Getter
@Setter
@NoArgsConstructor
public class SuperAdminProfile extends UserProfile {
}
