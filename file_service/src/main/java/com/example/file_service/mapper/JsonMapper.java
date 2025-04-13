package com.example.file_service.mapper;

import com.example.file_service.interfaces.Mapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class JsonMapper implements Mapper {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String serialize(Object objectToSerialize) {
        try {
            return mapper.writeValueAsString(objectToSerialize);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object deserialize(String json, Class<?> classToDeserialize) {
        try {
            return mapper.readValue(json, classToDeserialize);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
