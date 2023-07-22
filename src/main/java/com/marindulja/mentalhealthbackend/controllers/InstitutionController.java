package com.marindulja.mentalhealthbackend.controllers;

import com.marindulja.mentalhealthbackend.dtos.InstitutionDto;
import com.marindulja.mentalhealthbackend.models.SubscriptionStatus;
import com.marindulja.mentalhealthbackend.services.institutions.InstitutionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/institutions")
@PreAuthorize("hasRole('SUPERADMIN')")
public class InstitutionController {

    private final InstitutionService institutionService;

    public InstitutionController(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    @PostMapping("create")
    public ResponseEntity<?> createInstitution(@RequestBody InstitutionDto institutionDto) {
        InstitutionDto savedInstitution = institutionService.save(institutionDto);
        return new ResponseEntity<>(savedInstitution, HttpStatus.CREATED);
    }

    @PutMapping("{id}")
    public ResponseEntity<?> updateInstitution(@PathVariable Long id, @RequestBody InstitutionDto institutionDto) {
        InstitutionDto updatedInstitution = institutionService.update(id, institutionDto);
        return new ResponseEntity<>(updatedInstitution, HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getInstitutionById(@PathVariable Long id) {
        InstitutionDto institutionDto = institutionService.findById(id);
        return new ResponseEntity<>(institutionDto, HttpStatus.OK);
    }

    @GetMapping("all")
    public ResponseEntity<List<InstitutionDto>> getAllInstitutionsFilteredAndSorted(@RequestParam(name = "searchValue", required = false) String searchValue) {
        return new ResponseEntity<>(institutionService.getFilteredAndSorted(searchValue), HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteInstitution(@PathVariable Long id) {
        institutionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("{id}/subscription/activate")
    public ResponseEntity<?> activateSubscription(@PathVariable Long id) {
        institutionService.changeSubscriptionStatus(id, SubscriptionStatus.ACTIVE);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("{id}/subscription/cancel")
    public ResponseEntity<?> cancelSubscription(@PathVariable Long id) {
        institutionService.changeSubscriptionStatus(id, SubscriptionStatus.CANCELED);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
