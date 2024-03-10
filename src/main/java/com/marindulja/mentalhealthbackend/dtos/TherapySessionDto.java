package com.marindulja.mentalhealthbackend.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TherapySessionDto {

    private Long id;

    private UserReadDto therapist;

    private UserReadDto patient;

    private LocalDateTime sessionDate;
    private String patientNotes;

    private String therapistNotes;
}
