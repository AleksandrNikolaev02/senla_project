package com.example.demo.service;

import com.example.demo.annoration.Loggable;
import com.example.demo.annoration.Timed;
import com.example.demo.config.TopicConfig;
import com.example.demo.exception.JsonParseException;
import com.example.demo.exception.MicroserviceUnavailableException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.metric.CustomMetricService;
import com.example.dto.FileDataDTO;
import com.example.dto.Status;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@AllArgsConstructor
public class FileService {
    private ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;
    private TopicConfig config;
    private CustomMetricService customMetricService;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Integer TIMEOUT = 5;

    @Loggable(process = "получение файла из микросервиса file_service")
    @Timed
    public Resource getFile(String filePath) {
        RequestReplyFuture<String, String, String> future = sendSyncMessageToKafka(filePath);

        var response = getResponseFromFileMicroservice(future);

        FileDataDTO dto = parseFileData(response.value());

        validateStatus(dto.getStatus());

        return new ByteArrayResource(dto.getData());
    }

    private void validateStatus(Status status) {
        if (status != Status.OK) {
            throw new NotFoundException("File not found in MinIO service!");
        }
    }

    private FileDataDTO parseFileData(String json) {
        try {
            return mapper.readValue(json, FileDataDTO.class);
        } catch (JsonProcessingException e) {
            throw new JsonParseException("Error parse json from file_service microservice!");
        }
    }

    private RequestReplyFuture<String, String, String> sendSyncMessageToKafka(String filePath) {
        ProducerRecord<String, String> record = new ProducerRecord<>(config.getGetFileRequest(), filePath);

        return replyingKafkaTemplate.sendAndReceive(record);
    }

    private ConsumerRecord<String, String> getResponseFromFileMicroservice(
            RequestReplyFuture<String, String, String> future) {
        try {
            return future.get(TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            customMetricService.incrementKafkaErrorMetric();
            throw new MicroserviceUnavailableException("Microservice file_service is unavailable now!");
        }
    }
}
