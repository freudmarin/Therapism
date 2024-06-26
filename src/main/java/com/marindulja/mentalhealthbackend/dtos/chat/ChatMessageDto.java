package com.marindulja.mentalhealthbackend.dtos.chat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageDto {
    private Long senderId;
    private Long recipientId;
    private String content;

    // Constructors, getters, and setters
}
