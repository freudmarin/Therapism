package com.marindulja.mentalhealthbackend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("Therapist")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TherapistProfile extends UserProfile {
    private Integer yearsOfExperience;
    private String qualifications;

    @ManyToMany
    @JoinTable(
            name = "therapist_specializations",
            joinColumns = @JoinColumn(name = "therapist_profile_id"),
            inverseJoinColumns = @JoinColumn(name = "specialization_id")
    )
    private List<Specialization> specializations = new ArrayList<>();
}
