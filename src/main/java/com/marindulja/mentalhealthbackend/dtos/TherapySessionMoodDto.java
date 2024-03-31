package com.marindulja.mentalhealthbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class TherapySessionMoodDto {
    private Long patientId;
    private LocalDateTime sessionDate;
    private Integer moodBefore;
    private Integer moodDayOf;
    private Integer moodAfter;
    private LocalDateTime nearestEntryDateBefore;
    private LocalDateTime nearestEntryDateAfter;
}
