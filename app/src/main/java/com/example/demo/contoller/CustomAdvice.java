package com.example.demo.contoller;

import com.example.demo.exception.AnswerNotBelongsToCourseException;
import com.example.demo.exception.ArtifactNotBelongsToCourseException;
import com.example.demo.exception.FileStorageException;
import com.example.demo.exception.IncorrectArtifactTypeException;
import com.example.demo.exception.JsonParseException;
import com.example.demo.exception.KafkaSendMessageException;
import com.example.demo.exception.MicroserviceUnavailableException;
import com.example.demo.exception.NoRightsException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.TokenRefreshException;
import com.example.demo.exception.TwoFactorAuthenticationException;
import com.example.demo.metric.CustomMetricService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Slf4j
@AllArgsConstructor
public class CustomAdvice {
    private CustomMetricService customMetricService;
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> userNotFoundExceptionHandler(NotFoundException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NoRightsException.class)
    public ResponseEntity<String> noRightsExceptionHandler(NoRightsException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<String> noRightsExceptionHandler(FileStorageException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IncorrectArtifactTypeException.class)
    public ResponseEntity<String> exceptionHandler(IncorrectArtifactTypeException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ArtifactNotBelongsToCourseException.class)
    public ResponseEntity<String> exceptionHandler(ArtifactNotBelongsToCourseException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AnswerNotBelongsToCourseException.class)
    public ResponseEntity<String> exceptionHandler(AnswerNotBelongsToCourseException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
                log.error(error.getDefaultMessage());
                errors.put(error.getField(), error.getDefaultMessage());
            }
        );
        return errors;
    }

    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<String> exceptionHandler(TokenRefreshException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(JsonParseException.class)
    public ResponseEntity<String> exceptionHandler(JsonParseException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MicroserviceUnavailableException.class)
    public ResponseEntity<String> exceptionHandler(MicroserviceUnavailableException exception) {
        customMetricService.incrementErrorMetric();
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(TwoFactorAuthenticationException.class)
    public ResponseEntity<String> exceptionHandler(TwoFactorAuthenticationException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(KafkaSendMessageException.class)
    public ResponseEntity<String> exceptionHandler(KafkaSendMessageException exception) {
        customMetricService.incrementErrorMetric();
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
