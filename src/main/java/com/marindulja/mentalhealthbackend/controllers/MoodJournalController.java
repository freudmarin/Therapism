package com.marindulja.mentalhealthbackend.controllers;

import com.marindulja.mentalhealthbackend.dtos.moodjounral.MoodJournalReadDto;
import com.marindulja.mentalhealthbackend.dtos.moodjounral.MoodJournalWriteDto;
import com.marindulja.mentalhealthbackend.dtos.moodjounral.MoodTrendDto;
import com.marindulja.mentalhealthbackend.services.mood.MoodJournalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("api/v1/mood-journal")
@RequiredArgsConstructor
public class MoodJournalController {

    private final MoodJournalService moodEntryService;

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping
    public ResponseEntity<MoodJournalReadDto> createMoodEntry(@RequestBody MoodJournalWriteDto moodEntryDto) {
        final var createdMoodEntry = moodEntryService.createMoodEntry(moodEntryDto);
        return new ResponseEntity<>(createdMoodEntry, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('THERAPIST','PATIENT')")
    @GetMapping("patients/{patientId}")
    public ResponseEntity<List<MoodJournalReadDto>> getMoodJournalsByPatient(@PathVariable Long patientId) {
        final var moodEntries = moodEntryService.getMoodJournalsByPatient(patientId);
        return new ResponseEntity<>(moodEntries, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('THERAPIST')")
    @GetMapping("therapist")
    public ResponseEntity<List<MoodJournalReadDto>> getMoodJournalsByTherapist() {
        final var moodEntries = moodEntryService.getMoodJournalsByTherapist();
        return new ResponseEntity<>(moodEntries, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PutMapping("{moodJournalId}")
    public ResponseEntity<MoodJournalReadDto> updateMoodJournal(@PathVariable Long moodJournalId, @RequestBody MoodJournalWriteDto updatedMoodJournalDto) {
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
    @GetMapping("trends/{patientId}")
    public ResponseEntity<List<MoodTrendDto>> getMoodTrends(@PathVariable Long patientId, @RequestParam ChronoUnit interval) {
        final var moodTrends = moodEntryService.getMoodTrends(patientId, interval);
        return new ResponseEntity<>(moodTrends, HttpStatus.OK);
    }
}
