package com.marindulja.mentalhealthbackend.controllers;

import com.marindulja.mentalhealthbackend.dtos.TherapySessionReadDto;
import com.marindulja.mentalhealthbackend.dtos.TherapySessionWriteDto;
import com.marindulja.mentalhealthbackend.services.therapysession.TherapySessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("api/v1/therapySessions")
public class TherapyController {
    private final TherapySessionService therapySessionService;

    public TherapyController(TherapySessionService therapySessionService) {
        this.therapySessionService = therapySessionService;
    }
    @GetMapping("all")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<?> getAllSessionsOfTherapist(@RequestParam("from") LocalDateTime from,
                                                       @RequestParam("to") LocalDateTime to) {
        var allSessionsOfTherapist = therapySessionService.allSessionsOfTherapist(from, to);
        return new ResponseEntity<>(allSessionsOfTherapist, HttpStatus.OK);
    }

    @PostMapping("patient/{therapistId}/create")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<TherapySessionReadDto> createTherapySession(@PathVariable Long therapistId, @RequestBody TherapySessionWriteDto request) {
        var createdTherapySession = therapySessionService.createTherapySession(therapistId, request);
        return new ResponseEntity<>(createdTherapySession, HttpStatus.CREATED);
    }

    @PutMapping("therapy/{therapyId}/patient/{patientId}")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<?> updateExistingTherapySession(@PathVariable Long therapyId, @PathVariable Long patientId,
                                                          @RequestBody TherapySessionWriteDto request,
                                                          @RequestParam(value = "zoomCode", required = true) String zoomCode) {
        var updatedTherapySession = therapySessionService.updateTherapySession(patientId, therapyId, request, zoomCode);
        return new ResponseEntity<>(updatedTherapySession, HttpStatus.OK);
    }

    @PatchMapping("/session/{sessionId}/accept")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<TherapySessionReadDto> acceptSession(@PathVariable Long sessionId, @RequestParam(value = "zoomCode", required = true) String zoomCode) {
        var acceptedSession = therapySessionService.acceptSession(sessionId, zoomCode);
        return new ResponseEntity<>(acceptedSession, HttpStatus.OK);
    }

    @PatchMapping("/session/{sessionId}/decline")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<?> declineSession(@PathVariable Long sessionId) {
        therapySessionService.declineSession(sessionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("{therapyId}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> updatePatientNotes(@PathVariable Long therapyId, @RequestBody TherapySessionWriteDto therapySessionDto) {
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
