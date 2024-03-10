package com.marindulja.mentalhealthbackend.services.chat_messages;

import com.marindulja.mentalhealthbackend.dtos.ChatMessageDto;

import java.util.List;

public interface ChatMessageService {
    List<ChatMessageDto> getChatMessages(Long senderId, Long recipientId);
    ChatMessageDto saveMessage(ChatMessageDto messageDTO);
}
