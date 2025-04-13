package com.example.file_service.handler;

import com.example.dto.FileDataDTO;
import com.example.dto.Status;
import com.example.file_service.interfaces.Mapper;
import com.example.file_service.metric.CustomMetricService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component(value = "customKafkaErrorHandler")
@Slf4j
public class KafkaErrorHandler implements KafkaListenerErrorHandler {
    private final CustomMetricService customMetricService;
    private final Mapper mapper;

    public KafkaErrorHandler(CustomMetricService customMetricService,
                             @Qualifier(value = "jsonMapper") Mapper mapper) {
        this.customMetricService = customMetricService;
        this.mapper = mapper;
    }

    @NotNull
    @Override
    @SneakyThrows
    public Object handleError(@NotNull Message<?> message, ListenerExecutionFailedException exception) {
        Throwable cause = exception.getCause();

        log.error("Исключение {} с ошибкой: {}", cause, exception.getMessage());

        if (cause instanceof JsonProcessingException) {
            log.error("Ошибка сериализации/десериализации объекта!");
        } else {
            log.error("Ошибка при работе с сервисом Minio!");
        }

        customMetricService.incrementErrorMinioServiceMetric();

        return mapper.serialize(createFileDataDTO());
    }

    private FileDataDTO createFileDataDTO() {
        return new FileDataDTO(null, Status.NOT_FOUND);
    }
}
