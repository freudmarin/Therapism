package com.marindulja.mentalhealthbackend.controllers;

import com.marindulja.mentalhealthbackend.dtos.ChatMessageDto;
import com.marindulja.mentalhealthbackend.services.chat_messages.ChatMessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatMessageService chatMessageService;

    public ChatController(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessageDto>> getChatMessages(
            @RequestParam Long senderId, @RequestParam Long recipientId) {
        List<ChatMessageDto> messages = chatMessageService.getChatMessages(senderId, recipientId);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    @PostMapping("/send")
    public ResponseEntity<ChatMessageDto> sendMessage(@RequestBody ChatMessageDto messageDTO) {
        ChatMessageDto savedMessageDTO = chatMessageService.saveMessage(messageDTO);
        return new ResponseEntity<>(savedMessageDTO, HttpStatus.OK);
    }
}
