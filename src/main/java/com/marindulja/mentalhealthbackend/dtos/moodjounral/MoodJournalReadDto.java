package com.marindulja.mentalhealthbackend.dtos.moodjounral;

import com.marindulja.mentalhealthbackend.models.MoodType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MoodJournalReadDto {
    private Long id;
    private LocalDateTime entryDate;
    private Integer moodLevel;
    private MoodType moodType;
    private String thoughts;
    private String activities;
    private String aiNotes;
}
