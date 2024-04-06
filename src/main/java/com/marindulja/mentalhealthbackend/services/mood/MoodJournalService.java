package com.marindulja.mentalhealthbackend.services.mood;

import com.marindulja.mentalhealthbackend.dtos.MoodJournalReadDto;
import com.marindulja.mentalhealthbackend.dtos.MoodJournalWriteDto;
import com.marindulja.mentalhealthbackend.dtos.MoodTrendDto;

import java.time.temporal.ChronoUnit;
import java.util.List;

public interface MoodJournalService {
    MoodJournalReadDto createMoodEntry(MoodJournalWriteDto moodEntryDTO);

    List<MoodJournalReadDto> getMoodJournalsByTherapist();

    List<MoodJournalReadDto> getMoodJournalsByPatient(Long patientId);

    void deleteMoodEntry(Long moodEntryId);

    MoodJournalReadDto updateMoodJournal(Long moodEntryId, MoodJournalWriteDto updatedMoodEntryDTO);

    List<MoodTrendDto> getMoodTrends(Long userId, ChronoUnit interval);
}
