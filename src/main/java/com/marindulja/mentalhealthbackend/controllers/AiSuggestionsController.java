package com.marindulja.mentalhealthbackend.controllers;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/suggestions")
public class AiSuggestionsController {

    private final ChatClient chatClient;


    public AiSuggestionsController(ChatClient.Builder builder) {
        this.chatClient = builder
                .build();
    }

    @GetMapping("meditationExercise")
    @PreAuthorize("hasRole('PATIENT')")
    public String getMeditationExercise(@RequestParam(value = "topic", defaultValue = "Mindfulness") String topic) {
        String message = """
                Generate a meditation exercise for {topic}
                """;

        return chatClient.prompt()
                .user(u -> u.text(message).param("topic", topic))
                .call()
                .content();
    }

    @GetMapping("meditationTechniques")
    @PreAuthorize("hasRole('PATIENT')")
    public List<String> getMeditationTechniques(@RequestParam(value = "topic", defaultValue = "Mindfulness Meditation") String topic) {
        String message = """
                Generate a list of 10 meditation techniques for {topic}.
                """;
        return chatClient.prompt()
                .user(u -> u.text(message).param("topic", topic))
                .call()
                .entity(new ParameterizedTypeReference<>() {
                });
    }
}
