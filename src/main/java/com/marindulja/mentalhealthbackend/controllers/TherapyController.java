package com.marindulja.mentalhealthbackend.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.marindulja.mentalhealthbackend.dtos.therapysession.TherapySessionMoodDto;
import com.marindulja.mentalhealthbackend.dtos.therapysession.TherapySessionReadDto;
import com.marindulja.mentalhealthbackend.dtos.therapysession.TherapySessionWriteDto;
import com.marindulja.mentalhealthbackend.services.therapysessions.TherapySessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v1/therapySessions")
@RequiredArgsConstructor
public class TherapyController {
    private final TherapySessionService therapySessionService;

    @GetMapping("all")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<?> getAllSessionsOfTherapist(@RequestParam("from") LocalDateTime from,
                                                       @RequestParam("to") LocalDateTime to) {
        Map<String, List<TherapySessionReadDto>> allSessionsOfTherapist = null;
        try {
            allSessionsOfTherapist = therapySessionService.allSessionsOfTherapist(from, to);
        } catch (JsonProcessingException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
                                                          @RequestParam(value = "zoomCode") String zoomCode) {
        var updatedTherapySession = therapySessionService.updateTherapySession(patientId, therapyId, request, zoomCode);
        return new ResponseEntity<>(updatedTherapySession, HttpStatus.OK);
    }

    @PatchMapping("session/{sessionId}/accept")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<TherapySessionReadDto> acceptSession(@PathVariable Long sessionId, @RequestParam(value = "zoomCode") String zoomCode) {
        var acceptedSession = therapySessionService.acceptSession(sessionId, zoomCode);
        return new ResponseEntity<>(acceptedSession, HttpStatus.OK);
    }

    @GetMapping("session/{sessionId}")
    @PreAuthorize("hasAnyRole('THERAPIST', 'PATIENT')")
    public ResponseEntity<TherapySessionReadDto> getTherapySession(@PathVariable Long sessionId) {
        var therapySession = therapySessionService.getTherapySession(sessionId);
        return new ResponseEntity<>(therapySession, HttpStatus.OK);
    }

    @PatchMapping("session/{sessionId}/decline")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<?> declineSession(@PathVariable Long sessionId) {
        therapySessionService.declineSession(sessionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("{therapyId}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<TherapySessionReadDto> updatePatientNotes(@PathVariable Long therapyId, @RequestBody TherapySessionWriteDto therapySessionDto) {
        var therapySessionReadDto = therapySessionService.updatePatientNotes(therapyId, therapySessionDto);
        return new ResponseEntity<>(therapySessionReadDto, HttpStatus.OK);
    }

    @DeleteMapping("{therapyId}")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<?> deleteTherapySession(@PathVariable Long therapyId) {
        therapySessionService.deleteTherapySession(therapyId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("moodChanges/{patientId}")
    @PreAuthorize("hasAnyRole('THERAPIST','PATIENT')")
    public ResponseEntity<List<TherapySessionMoodDto>> findMoodChangesAroundTherapySessions(@PathVariable Long patientId) {
        var moodChangesAroundTherapySessions = therapySessionService.findMoodChangesAroundTherapySessions(patientId);
        return new ResponseEntity<>(moodChangesAroundTherapySessions, HttpStatus.OK);
    }
}
