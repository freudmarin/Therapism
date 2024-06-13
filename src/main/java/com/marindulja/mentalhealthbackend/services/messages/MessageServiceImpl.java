package com.marindulja.mentalhealthbackend.services.messages;

import com.marindulja.mentalhealthbackend.models.Message;
import com.marindulja.mentalhealthbackend.repositories.MessageRepository;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
public class MessageServiceImpl {
    private static final String ALGORITHM = "AES";

    private final MessageRepository messageRepository;

    private SecretKey secretKey;

    public MessageServiceImpl(MessageRepository messageRepository) throws Exception {
        this.messageRepository = messageRepository;
        // Generate a new AES key
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(256);
        this.secretKey = keyGen.generateKey();
    }

    public List<Message> getMessages(Long userId) {
        return messageRepository.findBySenderIdOrRecipientId(userId, userId);
    }

    public Message sendMessage(Long senderId, Long recipientId, String content) throws Exception {
        String encryptedContent = encrypt(content);
        Message message = new Message();
        message.setSenderId(senderId);
        message.setRecipientId(recipientId);
        message.setContent(encryptedContent);
        message.setTimestamp(LocalDateTime.now());
        return messageRepository.save(message);
    }

    private String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    private String decrypt(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decoded = Base64.getDecoder().decode(encryptedData);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted);
    }
}
