package com.marindulja.mentalhealthbackend.dtos.moodjounral;

import com.marindulja.mentalhealthbackend.models.MoodType;
import com.marindulja.mentalhealthbackend.validations.annotations.Between;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MoodJournalWriteDto {
    private LocalDateTime entryDate;
    @Between(min = 0, max = 10, message = "Mood level must be between 0 and 10")
    private Integer moodLevel;
    private MoodType moodType;
    private String thoughts;
    private String activities;
}
