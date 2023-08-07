package com.marindulja.mentalhealthbackend.dtos;

import com.marindulja.mentalhealthbackend.models.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TherapySessionDto {

    private Long id;

    private User therapist;

    private User patient;

    private LocalDateTime sessionDate;
    private String patientNotes;

    private String therapistNotes;
}
