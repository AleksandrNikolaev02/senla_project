package com.example.demo.service;

import com.example.demo.annoration.Loggable;
import com.example.demo.dto.MessageDTO;
import com.example.demo.mapper.MessageMapper;
import com.example.demo.model.Message;
import com.example.demo.model.User;
import com.example.demo.repository.MessageRepository;
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
    private final MessageMapper mapper;
    private EntityFindService entityFindService;

    @CacheEvict(value = {"messages", "correspondence"}, allEntries = true)
    @Loggable(process = "отправка сообщения пользователю")
    public void sendMessage(MessageDTO dto) {
        User sender = entityFindService.findUserById(dto.getSenderId());
        User recipient = entityFindService.findUserById(dto.getRecipientId());

        Message message = createMessageEntity(dto, sender, recipient);

        messageRepository.save(message);
    }

    private Message createMessageEntity(MessageDTO dto, User sender, User recipient) {
        return Message.builder()
                .content(dto.getContent())
                .createdAt(LocalDateTime.now())
                .sender(sender)
                .recipient(recipient).build();
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
