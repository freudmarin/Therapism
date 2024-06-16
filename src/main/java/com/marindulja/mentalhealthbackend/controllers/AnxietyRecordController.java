package com.marindulja.mentalhealthbackend.controllers;

import com.marindulja.mentalhealthbackend.dtos.anxietyrecord.AnxietyRecordReadDto;
import com.marindulja.mentalhealthbackend.dtos.anxietyrecord.AnxietyRecordWriteDto;
import com.marindulja.mentalhealthbackend.services.anxiety_records.AnxietyRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/anxiety-records")
@PreAuthorize("hasAnyRole('PATIENT', 'THERAPIST')")
@RequiredArgsConstructor
public class AnxietyRecordController {

    private final AnxietyRecordService anxietyRecordService;

    @PostMapping()
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> registerAnxietyLevels(@RequestBody AnxietyRecordWriteDto anxietyRecordDto) {
        anxietyRecordService.registerAnxietyLevels(anxietyRecordDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("{recordId}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> updateAnxietyRecord(@RequestBody AnxietyRecordWriteDto anxietyRecord, @PathVariable(name = "recordId") Long recordId) {
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
    public ResponseEntity<?> viewPatientAnxietyLevels(@PathVariable(name = "patientId") Long patientId) {
        final var anxietyRecordsList = anxietyRecordService.viewPatientAnxietyLevels(patientId);
        return new ResponseEntity<>(anxietyRecordsList, HttpStatus.OK);
    }
}
