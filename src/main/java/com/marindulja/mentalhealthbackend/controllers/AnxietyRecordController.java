package com.marindulja.mentalhealthbackend.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marindulja.mentalhealthbackend.dtos.anxietyrecord.AnxietyRecordReadDto;
import com.marindulja.mentalhealthbackend.dtos.anxietyrecord.AnxietyRecordWriteDto;
import com.marindulja.mentalhealthbackend.services.anxiety_records.AnxietyRecordService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/anxiety-records")
@PreAuthorize("hasAnyRole('PATIENT', 'THERAPIST')")
public class AnxietyRecordController {

    private final AnxietyRecordService anxietyRecordService;
    private final ObjectMapper objectMapper;
    private final ChatClient chatClient;

    public AnxietyRecordController(AnxietyRecordService anxietyRecordService, ObjectMapper objectMapper, ChatClient.Builder builder) {
        this.anxietyRecordService = anxietyRecordService;
        this.objectMapper = objectMapper;
        this.chatClient = builder.build();
    }

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Void> registerAnxietyLevels(@RequestBody AnxietyRecordWriteDto anxietyRecordDto) {
        anxietyRecordService.registerAnxietyLevels(anxietyRecordDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("{recordId}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Void> updateAnxietyRecord(@RequestBody AnxietyRecordWriteDto anxietyRecord, @PathVariable(name = "recordId") Long recordId) {
        anxietyRecordService.updateAnxietyRecord(anxietyRecord, recordId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("current-patient")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<AnxietyRecordReadDto>> getAllRecordsOfCurrentPatient() {
        final var anxietyRecords = anxietyRecordService.getAllOfCurrentPatient();
        return new ResponseEntity<>(anxietyRecords, HttpStatus.OK);
    }

    @GetMapping("{patientId}")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<List<AnxietyRecordReadDto>> viewPatientAnxietyLevels(@PathVariable(name = "patientId") Long patientId) {
        final var anxietyRecordsList = anxietyRecordService.viewPatientAnxietyLevels(patientId);
        return new ResponseEntity<>(anxietyRecordsList, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('PATIENT', 'THERAPIST')")
    @GetMapping("predict/{patientId}")
    public ResponseEntity<String> predictAnxietyAttack(@PathVariable Long patientId) {
        var anxietyRecords = anxietyRecordService.viewPatientAnxietyLevels(patientId);
        String anxietyRecordsJson;
        try {
            anxietyRecordsJson = objectMapper.writeValueAsString(anxietyRecords);
        } catch (JsonProcessingException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String message = """
                Predict the likelihood of an anxiety attack based on these records: {anxietyRecords}
                """;

        var prediction = chatClient.prompt()
                .user(u -> u.text(message).param("anxietyRecords", anxietyRecordsJson))
                .call().content();

        return new ResponseEntity<>(prediction, HttpStatus.OK);
    }
}
