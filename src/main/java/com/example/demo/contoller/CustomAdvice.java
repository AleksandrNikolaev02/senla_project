package com.example.demo.contoller;

import com.example.demo.exception.AnswerNotBelongsToCourseException;
import com.example.demo.exception.ArtifactNotBelongsToCourseException;
import com.example.demo.exception.FileStorageException;
import com.example.demo.exception.IncorrectArtifactTypeException;
import com.example.demo.exception.NoRightsException;
import com.example.demo.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class CustomAdvice {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> userNotFoundExceptionHandler(NotFoundException exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NoRightsException.class)
    public ResponseEntity<String> noRightsExceptionHandler(NoRightsException exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<String> noRightsExceptionHandler(FileStorageException exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IncorrectArtifactTypeException.class)
    public ResponseEntity<String> exceptionHandler(IncorrectArtifactTypeException exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ArtifactNotBelongsToCourseException.class)
    public ResponseEntity<String> exceptionHandler(ArtifactNotBelongsToCourseException exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AnswerNotBelongsToCourseException.class)
    public ResponseEntity<String> exceptionHandler(AnswerNotBelongsToCourseException exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
