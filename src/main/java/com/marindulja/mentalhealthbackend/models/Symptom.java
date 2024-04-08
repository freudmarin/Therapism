package com.marindulja.mentalhealthbackend.models;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "symptoms")
public class Symptom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @ManyToMany(mappedBy = "symptoms")
    private Set<PatientProfile> userProfiles = new HashSet<>();

    // ... constructors, getters, and setters ...
}
