package com.marindulja.mentalhealthbackend.controllers;

import com.marindulja.mentalhealthbackend.models.Message;
import com.marindulja.mentalhealthbackend.services.messages.MessageServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ChatController {

    private final MessageServiceImpl messageService;

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/sendMessage")
    @SendTo("/topic/messages")
    public Message sendMessage(Message message, SimpMessageHeaderAccessor headerAccessor) throws Exception {
        log.info("Message received from Front End: {}", message);
        message.setTimestamp(LocalDateTime.now());
        messageService.sendMessage(message.getSenderId(), message.getRecipientId(), message.getContent());
        return message;
    }

    @MessageMapping("/chat")
    public void chat(Message message) throws Exception {
        message.setTimestamp(LocalDateTime.now());
        messagingTemplate.convertAndSendToUser(message.getRecipientId().toString(), "/queue/messages", message);
        messageService.sendMessage(message.getSenderId(), message.getRecipientId(), message.getContent());
    }
}
