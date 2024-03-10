package com.marindulja.mentalhealthbackend.services.chat_messages;

import com.marindulja.mentalhealthbackend.dtos.ChatMessageDto;
import com.marindulja.mentalhealthbackend.models.ChatMessage;
import com.marindulja.mentalhealthbackend.models.User;
import com.marindulja.mentalhealthbackend.repositories.ChatMessageRepository;
import com.marindulja.mentalhealthbackend.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public ChatMessageServiceImpl(ChatMessageRepository chatMessageRepository, UserRepository userRepository, ModelMapper modelMapper) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    public List<ChatMessageDto> getChatMessages(Long senderId, Long recipientId) {
        User sender = userRepository.findById(senderId).orElseThrow(() -> new EntityNotFoundException("User with id " + senderId + " not found"));
        User recipient = userRepository.findById(recipientId).orElseThrow(() -> new EntityNotFoundException("User with id " + recipientId + " not found"));
        List<ChatMessage> messages = chatMessageRepository.findBySenderAndRecipient(sender, recipient);

        return messages.stream()
                .map(message -> modelMapper.map(message, ChatMessageDto.class))
                .collect(Collectors.toList());
    }

    public ChatMessageDto saveMessage(ChatMessageDto messageDTO) {
        ChatMessage message = modelMapper.map(messageDTO, ChatMessage.class);
        message.setTimestamp(LocalDateTime.now());

        ChatMessage savedMessage = chatMessageRepository.save(message);

        return modelMapper.map(savedMessage, ChatMessageDto.class);
    }
}
