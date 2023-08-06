package com.marindulja.mentalhealthbackend.controllers;

import com.marindulja.mentalhealthbackend.dtos.AnxietyRecordDto;
import com.marindulja.mentalhealthbackend.services.anxiety_records.AnxietyRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/anxiety-records")
@PreAuthorize("hasAnyRole('PATIENT', 'THERAPIST')")
public class AnxietyRecordController {

    private final AnxietyRecordService anxietyRecordService;

    public AnxietyRecordController(AnxietyRecordService anxietyRecordService) {
        this.anxietyRecordService = anxietyRecordService;
    }

    @PostMapping("patient/register-anxiety")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> registerAnxietyLevels(@RequestBody AnxietyRecordDto anxietyRecordDto) {
        anxietyRecordService.registerAnxietyLevels(anxietyRecordDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("{id}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> viewAnxietyRecord(@PathVariable Long id) {
        anxietyRecordService.getById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("{patientId}")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<?> viewPatientAnxietyLevels(@PathVariable(name = "patientId") Long patientId) {
        anxietyRecordService.viewPatientAnxietyLevels(patientId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
