package com.marindulja.mentalhealthbackend.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marindulja.mentalhealthbackend.dtos.moodjounral.MoodJournalReadDto;
import com.marindulja.mentalhealthbackend.dtos.moodjounral.MoodJournalWriteDto;
import com.marindulja.mentalhealthbackend.dtos.moodjounral.MoodTrendDto;
import com.marindulja.mentalhealthbackend.services.mood.MoodJournalService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v1/mood-journal")
public class MoodJournalController {

    private final MoodJournalService moodEntryService;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    public MoodJournalController(MoodJournalService moodEntryService, ChatClient.Builder builder, ObjectMapper objectMapper) {
        this.moodEntryService = moodEntryService;
        this.chatClient = builder
                .build();
        this.objectMapper = objectMapper;
    }

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
    public ResponseEntity<Map<String, List<MoodTrendDto>>> getMoodTrends(@PathVariable Long patientId, @RequestParam ChronoUnit interval) {
        final var moodTrends = moodEntryService.getMoodTrends(patientId, interval);
        String message = """
                Interpret these mood trends {moodTrends}
                """;
        String moodTrendsJson;
        try {
            moodTrendsJson = objectMapper.writeValueAsString(moodTrends);
        } catch (JsonProcessingException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        var moodTrendsResult = new HashMap<String, List<MoodTrendDto>>();
        var aiInterpretation = chatClient.prompt()
                .user(u -> u.text(message).param("moodTrends", moodTrendsJson))
                .call().content();
        moodTrendsResult.put(aiInterpretation, moodTrends);

        return new ResponseEntity<>(moodTrendsResult, HttpStatus.OK);
    }
}
