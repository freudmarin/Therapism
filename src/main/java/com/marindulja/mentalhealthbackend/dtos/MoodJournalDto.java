package com.marindulja.mentalhealthbackend.dtos;

import com.marindulja.mentalhealthbackend.models.MoodType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
public class MoodJournalDto {
    private Long id;
    private Long userId;
    private LocalDateTime entryDate;
    private Integer moodLevel;
    private MoodType moodType;
    private String thoughts;
    private String activities;

}
