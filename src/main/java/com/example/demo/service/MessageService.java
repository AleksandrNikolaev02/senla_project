package com.example.demo.service;

import com.example.demo.dto.MessageDTO;
import com.example.demo.exception.NotFoundException;
import com.example.demo.mapper.MessageMapper;
import com.example.demo.model.Message;
import com.example.demo.model.User;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageMapper mapper;

    @CacheEvict(value = {"messages", "correspondence"}, allEntries = true)
    public void sendMessage(MessageDTO dto) {
        User sender = userRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new NotFoundException("Sender not found"));
        User recipient = userRepository.findById(dto.getRecipientId())
                .orElseThrow(() -> new NotFoundException("Recipient not found"));

        Message message = Message.builder()
                .content(dto.getContent())
                .created_at(LocalDateTime.now())
                .sender(sender)
                .recipient(recipient).build();

        messageRepository.save(message);
    }

    @Cacheable(value = "messages", key = "{#senderId, #recipientId, #pageable.pageNumber, #pageable.pageSize}")
    public Page<MessageDTO> getMessages(Integer senderId, Integer recipientId, Pageable pageable) {
        return messageRepository.findBySenderIdAndRecipientId(senderId, recipientId, pageable)
                .map(mapper::messageToMessageDTO);
    }

    @Cacheable(value = "correspondence", key = "{#senderId, #recipientId, #pageable.pageNumber, #pageable.pageSize}")
    public Page<MessageDTO> getCorrespondence(Integer senderId, Integer recipientId, Pageable pageable) {
        return messageRepository.getCorrespondence(senderId, recipientId, pageable)
                .map(mapper::messageToMessageDTO);
    }
}
