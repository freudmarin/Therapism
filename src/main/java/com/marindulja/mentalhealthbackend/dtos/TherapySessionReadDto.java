package com.marindulja.mentalhealthbackend.dtos;

import com.marindulja.mentalhealthbackend.models.SessionStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TherapySessionReadDto {

    private Long id;

    private UserReadDto therapist;

    private UserReadDto patient;

    private LocalDateTime sessionDate;
    private String patientNotes;

    private String therapistNotes;

    private SessionStatus sessionStatus;

    private String zoomJoinLinkUrl;

    private Long meetingId;
}
