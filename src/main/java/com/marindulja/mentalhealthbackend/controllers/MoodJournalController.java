package com.marindulja.mentalhealthbackend.controllers;

import com.marindulja.mentalhealthbackend.dtos.MoodJournalDto;
import com.marindulja.mentalhealthbackend.dtos.MoodTrendDto;
import com.marindulja.mentalhealthbackend.services.mood.MoodJournalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/v1/mood-journal")
public class MoodJournalController {

    private final MoodJournalService moodEntryService;

    public MoodJournalController(MoodJournalService moodEntryService) {
        this.moodEntryService = moodEntryService;
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping
    public ResponseEntity<MoodJournalDto> createMoodEntry(@RequestBody MoodJournalDto moodEntryDto) {
        final var createdMoodEntry = moodEntryService.createMoodEntry(moodEntryDto);
        return new ResponseEntity<>(createdMoodEntry, HttpStatus.CREATED);
    }
    @PreAuthorize("hasAnyRole('THERAPIST','PATIENT')")
    @GetMapping("patients/{userId}")
    public ResponseEntity<List<MoodJournalDto>> getMoodJournalsByPatient(@PathVariable Long userId) {
        final var moodEntries = moodEntryService.getMoodJournalsByPatient(userId);
        return new ResponseEntity<>(moodEntries, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('THERAPIST')")
    @GetMapping("therapists/{therapistId}")
    public ResponseEntity<List<MoodJournalDto>> getMoodJournalsByTherapist() {
        final var moodEntries = moodEntryService.getMoodJournalsByTherapist();
        return new ResponseEntity<>(moodEntries, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PutMapping("{moodJournalId}")
    public ResponseEntity<MoodJournalDto> updateMoodJournal(@PathVariable Long moodJournalId, @RequestBody MoodJournalDto updatedMoodJournalDto) {
        final var updatedMoodEntry = moodEntryService.updateMoodJournal(moodJournalId, updatedMoodJournalDto);
        return new ResponseEntity<>(updatedMoodEntry, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @DeleteMapping("{moodJournalId}")
    public ResponseEntity<Void> deleteMoodEntry(@PathVariable Long moodJournalId) {
        moodEntryService.deleteMoodEntry(moodJournalId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('PATIENT', 'THERAPIST')")
    @GetMapping("trends/{userId}")
    public ResponseEntity<List<MoodTrendDto>> getMoodTrends(@PathVariable Long userId, @RequestParam ChronoUnit interval) {
        final var moodTrends = moodEntryService.getMoodTrends(userId, interval);
        return new ResponseEntity<>(moodTrends, HttpStatus.OK);
    }
}
