package com.marindulja.mentalhealthbackend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "mood_journals")
@Getter
@Setter
@SQLRestriction("is_deleted <> 1")
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

    @Column(name = "ai_notes", columnDefinition = "TEXT")
    private String aiNotes;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @Column(name = "shared_with_therapist")
    private boolean sharedWithTherapist;

    // Additional details related to mood assessment

    // Getters and setters
}
