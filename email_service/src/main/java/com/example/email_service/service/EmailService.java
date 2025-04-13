package com.example.email_service.service;

import com.example.dto.CheckEmailDTO;
import com.example.dto.Status;
import com.example.dto.TwoFactorCodeDTO;
import com.example.email_service.model.TwoFactorCode;
import com.example.email_service.repository.TwoFactorCodeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@AllArgsConstructor
@Slf4j
public class EmailService {
    private JavaMailSender sender;
    private TwoFactorCodeRepository twoFactorCodeRepository;
    private final ObjectMapper mapper = new ObjectMapper();
    private final int MIN_VALUE_TWO_FACTOR_CODE = 1000;
    private final int MAX_VALUE_TWO_FACTOR_CODE = 9999;
    private final int MAX_TWO_FACTOR_CODE_LIFETIME = 2;

    @KafkaListener(topics = "${topics.email-request}", groupId = "${spring.kafka.consumer.group-id}")
    public void sendEmail(String login) {
        log.info("Получен запрос от основного микросервиса.");

        Integer code = generateTwoFactorCode();
        saveTwoFactorCode(login, code);
        sendTwoFactorEmail(login, code);
    }

    @SneakyThrows
    @KafkaListener(topics = "${topics.get-email}",
                   groupId = "${spring.kafka.consumer.group-id}",
                   errorHandler = "errorHandler",
                   concurrency = "5")
    @SendTo("#{topicConfig.getGetEmailReply()}")
    public String checkTwoFactorCode(String json) {
        log.info("Получен запрос от основного микросервиса.");

        TwoFactorCodeDTO dto = parseDtoFromString(json);

        var twoFactorCodes = getTwoFactorCodesByUserLogin(dto.getLogin());

        return handleTwoFactorCodes(twoFactorCodes, dto);
    }

    @Scheduled(fixedRateString = "${scheduled.time-clean-db}")
    @Async
    public void cleanAllExpiredCodes() {
        log.info("It is time to clean up the database!");

        int count = twoFactorCodeRepository.deleteAllExpiredCodes(LocalDateTime.now());

        log.info("Deleted {} records", count);
    }

    private Integer generateTwoFactorCode() {
        Random random = new Random();

        return random.nextInt(MIN_VALUE_TWO_FACTOR_CODE, MAX_VALUE_TWO_FACTOR_CODE);
    }

    private void saveTwoFactorCode(String login, Integer code) {
        TwoFactorCode twoFactorCode = TwoFactorCode.builder()
                .login(login)
                .code(code)
                .expiredAt(LocalDateTime.now().plusMinutes(MAX_TWO_FACTOR_CODE_LIFETIME))
                .build();

        twoFactorCodeRepository.save(twoFactorCode);
    }

    private void sendTwoFactorEmail(String login, Integer code) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(login);
        message.setSubject("From project");
        message.setText("Your signIn code: " + code);

        log.info("Отправляю письмо по адресу: {}", login);

        sender.send(message);
    }

    private TwoFactorCodeDTO parseDtoFromString(String jsonStr) throws JsonProcessingException {
        return mapper.readValue(jsonStr, TwoFactorCodeDTO.class);
    }

    private List<TwoFactorCode> getTwoFactorCodesByUserLogin(String login) {
        return twoFactorCodeRepository.findByLogin(login);
    }

    private String handleTwoFactorCodes(List<TwoFactorCode> twoFactorCodes, TwoFactorCodeDTO dto)
            throws JsonProcessingException {
        for (TwoFactorCode code : twoFactorCodes) {
            if (checkValidTwoFactorCode(code, dto.getCode())) {
                log.info("Код пользователя совпал с кодом из БД!");
                return createCheckEmailDTO(Status.OK, dto.getLogin());
            }
        }

        log.info("Код пользователя не совпал с кодом из БД!");
        return createCheckEmailDTO(Status.UNAUTHORIZED, null);
    }

    private boolean checkValidTwoFactorCode(TwoFactorCode dbCode, Integer dtoCode) {
        return dbCode.getCode().equals(dtoCode) &&
                dbCode.getExpiredAt().compareTo(LocalDateTime.now()) > 0;
    }

    private String createCheckEmailDTO(Status status, String email)
            throws JsonProcessingException {
        CheckEmailDTO checkEmailDTO = new CheckEmailDTO();
        checkEmailDTO.setStatus(status);
        checkEmailDTO.setEmail(email);
        return mapper.writeValueAsString(checkEmailDTO);
    }
}
