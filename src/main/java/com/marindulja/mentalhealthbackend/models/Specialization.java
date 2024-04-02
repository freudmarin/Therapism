package com.marindulja.mentalhealthbackend.models;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "specializations")
public class Specialization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToMany(mappedBy = "specializations")
    private List<TherapistProfile> therapists = new ArrayList<>();

    // Constructor, getters, and setters
}
