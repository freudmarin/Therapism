package com.marindulja.mentalhealthbackend.controllers;

import com.marindulja.mentalhealthbackend.dtos.AnxietyRecordReadDto;
import com.marindulja.mentalhealthbackend.dtos.AnxietyRecordWriteDto;
import com.marindulja.mentalhealthbackend.dtos.PatientProfileReadDto;
import com.marindulja.mentalhealthbackend.services.anxiety_records.AnxietyRecordService;
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

    public AnxietyRecordController(AnxietyRecordService anxietyRecordService) {
        this.anxietyRecordService = anxietyRecordService;
    }

    @PostMapping("current-user/register-anxiety")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PatientProfileReadDto> registerAnxietyLevels(@RequestBody AnxietyRecordWriteDto anxietyRecordDto) {
        final var userProfileDto = anxietyRecordService.registerAnxietyLevels(anxietyRecordDto);
        return new ResponseEntity<>(userProfileDto, HttpStatus.OK);
    }

    @GetMapping("current-user")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<AnxietyRecordReadDto>> getAllRecordsOfCurrentUser() {
        final var anxietyRecords = anxietyRecordService.getAllOfCurrentUser();
        return new ResponseEntity<>(anxietyRecords, HttpStatus.OK);
    }

    @GetMapping("{patientId}")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<?> viewPatientAnxietyLevels(@PathVariable(name = "patientId") Long patientId) {
        final var anxietyRecordsList = anxietyRecordService.viewPatientAnxietyLevels(patientId);
        return new ResponseEntity<>(anxietyRecordsList, HttpStatus.OK);
    }
}
