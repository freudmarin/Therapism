package com.marindulja.mentalhealthbackend.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Where;

import java.util.Set;

@Data
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

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @ManyToMany
    @JoinTable(
            name = "user_profile_disorders",
            joinColumns = @JoinColumn(name = "user_profile_id"),
            inverseJoinColumns = @JoinColumn(name = "disorder_id"))
    private Set<Disorder> disorders;
}
