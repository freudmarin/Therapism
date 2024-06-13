package com.marindulja.mentalhealthbackend.controllers;

import com.marindulja.mentalhealthbackend.dtos.DisorderDto;
import com.marindulja.mentalhealthbackend.dtos.MostCommonDisordersDto;
import com.marindulja.mentalhealthbackend.dtos.mapping.ModelMappingUtility;
import com.marindulja.mentalhealthbackend.models.Disorder;
import com.marindulja.mentalhealthbackend.repositories.DisorderRepository;
import com.marindulja.mentalhealthbackend.services.disorders.DisorderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/disorders")
@Slf4j
public class DisorderController {
    private final DisorderService disorderService;

    private final DisorderRepository disorderRepository;

    private final ChatClient chatClient;

    private final ModelMappingUtility mapper;

    public DisorderController(DisorderService disorderService, DisorderRepository disorderRepository, ChatClient.Builder builder, ModelMappingUtility mapper) {
        this.disorderService = disorderService;
        this.disorderRepository = disorderRepository;
        this.chatClient = builder.build();
        this.mapper = mapper;
    }

    @GetMapping("all")
    @PreAuthorize("hasAnyRole('THERAPIST', 'PATIENT', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<DisorderDto>> getAllDisorders() {
        var allDisorders = disorderService.getAllDisorders();
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
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @GetMapping("highAnxietyPatients")
    @PreAuthorize("hasAnyRole('THERAPIST', 'PATIENT', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<MostCommonDisordersDto>> findCommonDisordersAmongHighAnxietyPatients() {
        var mostCommonDisordersAmongHighAnxietyPatients = disorderService.findCommonDisordersAmongHighAnxietyPatients();
        return new ResponseEntity<>(mostCommonDisordersAmongHighAnxietyPatients, HttpStatus.OK);
    }

    @GetMapping("allByArtificialIntelligence")
    @PreAuthorize("hasAnyRole('THERAPIST', 'PATIENT', 'ADMIN', 'SUPERADMIN')")
    public List<DisorderDto> getAllDisordersFromArtificialIntelligence(
            @RequestParam(value = "message", defaultValue = "What are all the mental health disorders that exist?") String message
    ) {
        List<Disorder> disorders = chatClient.prompt()
                .user(message)
                .call()
                .entity(new ParameterizedTypeReference<>() {
                });
        List<Disorder> fromDb = disorderRepository.findAll();
        disorders.forEach(disorder -> {
            if (!fromDb.contains(disorder)) {
                disorderRepository.save(disorder);
            }
        });

        return disorders.stream().map(disorder -> mapper.map(disorder, DisorderDto.class))
                .collect(Collectors.toList());
    }
}
