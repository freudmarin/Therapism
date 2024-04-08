package com.marindulja.mentalhealthbackend.repositories;

import com.marindulja.mentalhealthbackend.models.ChatMessage;
import com.marindulja.mentalhealthbackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySenderAndRecipient(User sender, User recipient);

    List<ChatMessage> findBySenderOrRecipient(User user1, User user2);
}
