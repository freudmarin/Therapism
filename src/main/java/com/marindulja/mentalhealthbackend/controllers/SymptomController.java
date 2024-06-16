package com.marindulja.mentalhealthbackend.controllers;

import com.marindulja.mentalhealthbackend.services.symptoms.SymptomServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/symptoms")
@Slf4j
@RequiredArgsConstructor
public class SymptomController {

    private final SymptomServiceImpl symptomService;
}
