package com.example.email_service.handler;

import com.example.dto.CheckEmailDTO;
import com.example.dto.Status;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component(value = "errorHandler")
@Slf4j
public class EmailServiceErrorHandler implements KafkaListenerErrorHandler {
    private final ObjectMapper mapper = new ObjectMapper();

    @SneakyThrows
    @Override
    @NonNull
    public Object handleError(@NonNull Message<?> message, ListenerExecutionFailedException exception) {
        Throwable cause = exception.getCause();

        log.error("Выброшено исключение {} с сообщением {}", cause, exception.getMessage());

        return mapper.writeValueAsString(new CheckEmailDTO(Status.ERROR_JSON_PARSE, null));
    }
}
