package com.marindulja.mentalhealthbackend.controllers;

import com.marindulja.mentalhealthbackend.dtos.MedicationDto;
import com.marindulja.mentalhealthbackend.services.medications.MedicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/disorders")
@PreAuthorize("hasAnyRole('SUPERADMIN', 'THERAPIST')")
public class MedicationController {
    private final MedicationService medicationService;

    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @GetMapping("all")
    @PreAuthorize("hasAnyRole('THERAPIST', 'PATIENT', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<MedicationDto>> getAllDisorders() {
        List<MedicationDto> allDisorders = medicationService.getAllMedications();
        return new ResponseEntity<>(allDisorders, HttpStatus.OK);
    }


    @PutMapping("users/{userId}/assignMedications")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<?> assignMedicationsToUser(@PathVariable Long userId, @RequestBody List<Long> medicationIds) {
        medicationService.assignMedicationsToUser(userId, medicationIds);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("users/{userId}/updateMedications")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<?> updateMedicationsToUser(@PathVariable Long userId, @RequestBody List<Long> medicationIds) {
        medicationService.updateMedicationsToUser(userId, medicationIds);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("users/{userId}/removeMedications")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<?> removeMedicationsFromPatient(@PathVariable Long userId, @RequestBody List<Long> medicationIds) {
        medicationService.removeMedicationsFromPatient(userId, medicationIds);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
