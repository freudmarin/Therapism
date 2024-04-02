package com.marindulja.mentalhealthbackend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("Patient")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientProfile extends UserProfile {

    @ManyToMany
    @JoinTable(
            name = "patient_profile_symptoms",
            joinColumns = @JoinColumn(name = "patient_profile_id"),
            inverseJoinColumns = @JoinColumn(name = "symptom_id")
    )
    private List<Symptom> symptoms = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "patient_profile_disorders",
            joinColumns = @JoinColumn(name = "patient_profile_id"),
            inverseJoinColumns = @JoinColumn(name = "disorder_id"))
    private List<Disorder> disorders = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<AnxietyRecord> anxietyRecords = new ArrayList<>();

    public PatientProfile(Long id, String phoneNumber, Gender gender, boolean isDeleted, User user, List<Symptom> symptoms, List<Disorder> disorders, List<AnxietyRecord> anxietyRecords) {
        super(id, phoneNumber, gender, isDeleted, user);
        this.symptoms = symptoms;
        this.disorders = disorders;
        this.anxietyRecords = anxietyRecords;
    }

    // Getters and setters for the new fields
}
