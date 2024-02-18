package com.marindulja.mentalhealthbackend.services.mood;

import com.marindulja.mentalhealthbackend.dtos.MoodJournalDto;
import com.marindulja.mentalhealthbackend.dtos.MoodTrendDto;

import java.time.temporal.ChronoUnit;
import java.util.List;

public interface MoodJournalService {
    MoodJournalDto createMoodEntry(MoodJournalDto moodEntryDTO);

    List<MoodJournalDto> getMoodJournalsByTherapist();

    List<MoodJournalDto> getMoodJournalsByPatient(Long userId);

    void deleteMoodEntry(Long moodEntryId);

    MoodJournalDto updateMoodJournal(Long moodEntryId, MoodJournalDto updatedMoodEntryDTO);

    List<MoodTrendDto> getMoodTrends(Long userId, ChronoUnit interval);
}
