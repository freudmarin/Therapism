package com.marindulja.mentalhealthbackend.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "user_medications")
public class Medication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(mappedBy = "medications")
    private List<UserProfile> users = new ArrayList<>();

    private String name;
    private String description;
}
