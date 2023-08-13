package com.marindulja.mentalhealthbackend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "user_profiles")
@Where(clause = "is_deleted = false")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String name;
    private String surname;
    private String phoneNumber;

    private String triggers;


    private String medication_dosage;
    private String dosage_frequency;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @ManyToMany
    @JoinTable(
            name = "user_profile_disorders",
            joinColumns = @JoinColumn(name = "user_profile_id"),
            inverseJoinColumns = @JoinColumn(name = "disorder_id"))
    private List<Disorder> disorders = new ArrayList<>();


    @ManyToMany
    @JoinTable(
            name = "user_profile_medication",
            joinColumns = @JoinColumn(name = "user_profile_id"),
            inverseJoinColumns = @JoinColumn(name = "medication_id")
    )
    private List<Medication> medications = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "userprofile_anxietyrecords",
            joinColumns = @JoinColumn(name = "user_profile_id"),
            inverseJoinColumns = @JoinColumn(name = "anxiety_record_id")
    )
    private List<AnxietyRecord> anxietyRecords = new ArrayList<>();


}
