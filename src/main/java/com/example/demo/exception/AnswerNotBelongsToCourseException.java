package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class AnswerNotBelongsToCourseException extends RuntimeException {
    public AnswerNotBelongsToCourseException(String message) {
        super(message);
    }
}
