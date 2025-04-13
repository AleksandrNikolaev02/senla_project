package com.example.file_service.interfaces;

public interface Mapper {
    String serialize(Object objectToSerialize);
    Object deserialize(String json, Class<?> classToDeserialize);
}
