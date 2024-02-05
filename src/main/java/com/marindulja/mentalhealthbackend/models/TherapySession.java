package com.marindulja.mentalhealthbackend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
@Getter
@Setter
@Entity
@Table(name = "therapy_sessions")
@Where(clause = "is_deleted = false")
public class TherapySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "therapist_id", nullable = false)
    private User therapist;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    private LocalDateTime sessionDate;
    @Column(name = "patient_notes")
    private String patientNotes;

    @Column(name = "therapist_notes")
    private String therapistNotes;

    @Column(name = "is_deleted")
    private boolean isDeleted;
}
