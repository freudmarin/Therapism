package com.marindulja.mentalhealthbackend.controllers;

import com.marindulja.mentalhealthbackend.dtos.DisorderDto;
import com.marindulja.mentalhealthbackend.services.disorders.DisorderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/disorders")
@PreAuthorize("hasAnyRole('SUPERADMIN', 'THERAPIST')")
public class DisorderController {
    private final DisorderService disorderService;

    public DisorderController(DisorderService disorderService) {
        this.disorderService = disorderService;
    }

    @GetMapping("all")
    @PreAuthorize("hasAnyRole('THERAPIST', 'PATIENT', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<DisorderDto>> getAllDisorders() {
       List<DisorderDto> allDisorders = disorderService.getAllDisorders();
        return new ResponseEntity<>(allDisorders, HttpStatus.OK);
    }
    @PutMapping("users/{userId}/assignDisorders")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<?> assignDisordersToUser(@PathVariable Long userId, @RequestBody List<Long> disorderIds) {
        disorderService.assignDisordersToUser(userId, disorderIds);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("users/{userId}/updateDisorders")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<?> updateDisordersToUser(@PathVariable Long userId, @RequestBody List<Long> disorderIds) {
        disorderService.updateDisordersToUser(userId, disorderIds);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("users/{userId}/removeDisorders")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<?> removeDisordersFromPatient(@PathVariable Long userId, @RequestBody List<Long> disorderIds) {
        disorderService.removeDisordersFromPatient(userId, disorderIds);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
