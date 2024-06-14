package com.marindulja.mentalhealthbackend.controllers;

import com.marindulja.mentalhealthbackend.dtos.DisorderDto;
import com.marindulja.mentalhealthbackend.services.symptoms.SymptomServiceImpl;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/symptoms")
@Slf4j
@RequiredArgsConstructor
public class SymptomController {

    private final SymptomServiceImpl symptomService;

    @PostMapping("choose/{patientId}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<DisorderDto>> chooseSymptoms(@PathVariable Long patientId,
                                                            @RequestBody List<Long> symptomIds) {
         symptomService.chooseSymptoms(patientId, symptomIds);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
