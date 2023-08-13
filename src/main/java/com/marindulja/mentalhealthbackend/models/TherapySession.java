package com.marindulja.mentalhealthbackend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@Entity
@Table(name = "therapy_sessions")
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
}
