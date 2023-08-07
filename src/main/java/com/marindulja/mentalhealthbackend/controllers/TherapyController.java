package com.marindulja.mentalhealthbackend.controllers;

import com.marindulja.mentalhealthbackend.dtos.TherapySessionDto;
import com.marindulja.mentalhealthbackend.services.therapysession.TherapySessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("api/v1/therapySessions")
@PreAuthorize("hasAnyRole('PATIENT', 'THERAPIST')")
public class TherapyController {
    private final TherapySessionService therapySessionService;

    public TherapyController(TherapySessionService therapySessionService) {
        this.therapySessionService = therapySessionService;
    }


    @GetMapping("therapist/all")
    @PreAuthorize("hasAnyRole('THERAPIST')")
    public ResponseEntity<List<TherapySessionDto>> getAllTasksAssignedToUser(@RequestParam LocalDateTime startDate, @RequestParam LocalDateTime endDate) {
        List<TherapySessionDto> allSessionsOfTherapist = therapySessionService.allSessionsOfTherapist(startDate, endDate);
        return new ResponseEntity<>(allSessionsOfTherapist, HttpStatus.OK);
    }

    @PostMapping("patient/{patientId}/create")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<?> createTherapySession(@PathVariable Long patientId, @RequestBody TherapySessionDto request) {
        TherapySessionDto createdTherapySession = therapySessionService.createTherapySession(patientId, request);
        return new ResponseEntity<>(createdTherapySession, HttpStatus.OK);
    }

    @PutMapping("therapy/{therapyId}/patient/{patientId}")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<?> updateExistingTherapySession(@PathVariable Long therapyId, @PathVariable Long patientId, @RequestBody TherapySessionDto request) {
        TherapySessionDto updatedTherapySession = therapySessionService.updateTherapySession(patientId, therapyId, request);
        return new ResponseEntity<>(updatedTherapySession, HttpStatus.OK);
    }

    @PatchMapping("{therapyId}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> updatePatientNotes(@PathVariable Long therapyId, @RequestBody TherapySessionDto therapySessionDto) {
        therapySessionService.updatePatientNotes(therapyId, therapySessionDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
