package com.marindulja.mentalhealthbackend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

@Getter
@Setter
@Entity
@Table(name = "user_profiles")
@Where(clause = "is_deleted = false")
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_profile_type", discriminatorType = DiscriminatorType.STRING)
public class UserProfile {

    @Id
    private Long id;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    public UserProfile(Long id, String phoneNumber, Gender gender) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
    }
}
