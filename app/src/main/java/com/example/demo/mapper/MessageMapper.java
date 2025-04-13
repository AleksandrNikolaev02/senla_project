package com.example.demo.mapper;

import com.example.demo.dto.MessageDTO;
import com.example.demo.model.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(source = "recipient.id", target = "recipientId")
    MessageDTO messageToMessageDTO(Message message);
}
