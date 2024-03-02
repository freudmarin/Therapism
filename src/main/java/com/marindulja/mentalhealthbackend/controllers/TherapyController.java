package com.marindulja.mentalhealthbackend.controllers;

import com.marindulja.mentalhealthbackend.dtos.TherapySessionDto;
import com.marindulja.mentalhealthbackend.services.therapysession.TherapySessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/therapySessions")
public class TherapyController {
    private final TherapySessionService therapySessionService;

    public TherapyController(TherapySessionService therapySessionService) {
        this.therapySessionService = therapySessionService;
    }

    @PostMapping("patient/{patientId}/create")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> createTherapySession(@PathVariable Long therapistID, @RequestBody TherapySessionDto request) {
        var createdTherapySession = therapySessionService.createTherapySession(therapistID, request);
        return new ResponseEntity<>(createdTherapySession, HttpStatus.CREATED);
    }

    @PutMapping("therapy/{therapyId}/patient/{patientId}")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<?> updateExistingTherapySession(@PathVariable Long therapyId, @PathVariable Long patientId, @RequestBody TherapySessionDto request) {
        var updatedTherapySession = therapySessionService.updateTherapySession(patientId, therapyId, request);
        return new ResponseEntity<>(updatedTherapySession, HttpStatus.OK);
    }

    @PatchMapping("/session/{sessionId}/accept")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<?> acceptSession(@PathVariable Long sessionId) {
        therapySessionService.acceptSession(sessionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/session/{sessionId}/decline")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<?> declineSession(@PathVariable Long sessionId) {
        therapySessionService.declineSession(sessionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("{therapyId}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> updatePatientNotes(@PathVariable Long therapyId, @RequestBody TherapySessionDto therapySessionDto) {
        therapySessionService.updatePatientNotes(therapyId, therapySessionDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("{therapyId}")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<?> deleteTherapySession(@PathVariable Long therapyId) {
        therapySessionService.deleteTherapySession(therapyId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
