package com.marindulja.mentalhealthbackend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(name = "mood_journals")
@Getter
@Setter
@Where(clause = "is_deleted = false")
public class MoodJournal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserProfile user;

    private LocalDateTime entryDate;
    private Integer moodLevel;

    @Enumerated(EnumType.STRING)
    private MoodType moodType;

    @Column(name = "thoughts", columnDefinition = "TEXT")
    private String thoughts;

    @Column(name = "activities", columnDefinition = "TEXT")
    private String activities;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    // Additional details related to mood assessment

    // Getters and setters
}
