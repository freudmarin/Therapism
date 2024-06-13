package com.marindulja.mentalhealthbackend.controllers;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/faq")
@PreAuthorize("hasAnyRole('PATIENT', 'THERAPIST', 'ADMIN', 'SUPERADMIN')")
public class FAQController {

    private final ChatClient chatClient;

    private final VectorStore vectorStore;

    @Value("classpath:prompts/rag-prompt-template.st")
    private Resource ragPromptTemplate;

    public FAQController(ChatClient.Builder builder, VectorStore vectorStore, VectorStore vectorStore1) {
        this.vectorStore = vectorStore1;
        this.chatClient = builder.build();
    }

    @GetMapping("ask")
    public String faq(@RequestParam(value = "message", defaultValue = "What are mental disorders?") String message) {
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.query(message).withTopK(2));
        List<String> contentList = documents.stream().map(Document::getContent).toList();

        return chatClient.prompt()
                .user(u -> {
                            u.text(ragPromptTemplate);
                            u.param("input", message);
                            u.param("documents", String.join(",", contentList));
                        }
                ).call()
                .content();
    }
}
