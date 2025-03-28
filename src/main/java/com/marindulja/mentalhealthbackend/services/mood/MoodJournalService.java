package com.marindulja.mentalhealthbackend.services.mood;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.marindulja.mentalhealthbackend.dtos.moodjounral.MoodJournalReadDto;
import com.marindulja.mentalhealthbackend.dtos.moodjounral.MoodJournalWriteDto;
import com.marindulja.mentalhealthbackend.dtos.moodjounral.MoodTrendDto;

import java.time.temporal.ChronoUnit;
import java.util.List;

public interface MoodJournalService {
    MoodJournalReadDto createMoodEntry(MoodJournalWriteDto moodEntryDTO) throws JsonProcessingException;

    List<MoodJournalReadDto> getMoodJournalsByTherapist();

    List<MoodJournalReadDto> getMoodJournalsByPatient(Long patientId);

    void deleteMoodEntry(Long moodEntryId);

    MoodJournalReadDto updateMoodJournal(Long moodEntryId, MoodJournalWriteDto updatedMoodEntryDTO) throws JsonProcessingException;

    void shareMoodJournalWithTherapist(Long moodEntryId, Long therapistId);

    List<MoodTrendDto> getMoodTrends(Long patientId, ChronoUnit interval);
}
