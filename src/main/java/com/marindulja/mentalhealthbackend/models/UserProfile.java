package com.marindulja.mentalhealthbackend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "user_profiles")
@Where(clause = "is_deleted = false")
@NoArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phoneNumber;

    private String triggers;

    @Enumerated(EnumType.STRING)
    private Gender gender;

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
            name = "userprofile_anxietyrecords",
            joinColumns = @JoinColumn(name = "user_profile_id"),
            inverseJoinColumns = @JoinColumn(name = "anxiety_record_id")
    )
    private List<AnxietyRecord> anxietyRecords = new ArrayList<>();

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

}
