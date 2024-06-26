package com.marindulja.mentalhealthbackend.dtos.therapysession;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TherapySessionWriteDto {
    private LocalDateTime sessionDate;

    private String patientNotes;

    private String therapistNotes;
}
