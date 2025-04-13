package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
public class MicroserviceUnavailableException extends RuntimeException {
    public MicroserviceUnavailableException(String message) {
        super(message);
    }
}
