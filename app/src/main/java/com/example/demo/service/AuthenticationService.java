package com.example.demo.service;

import com.example.demo.annoration.Loggable;
import com.example.demo.annoration.Timed;
import com.example.demo.config.TopicConfig;
import com.example.demo.dto.LoginDTO;
import com.example.demo.dto.RegisterDTO;
import com.example.demo.dto.ResponseTokenRefreshDTO;
import com.example.demo.enums.Role;
import com.example.demo.exception.JsonParseException;
import com.example.demo.exception.KafkaSendMessageException;
import com.example.demo.exception.MicroserviceUnavailableException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.TwoFactorAuthenticationException;
import com.example.demo.metric.CustomMetricService;
import com.example.demo.model.RoleUser;
import com.example.demo.model.User;
import com.example.demo.model.UserSetting;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserSettingRepository;
import com.example.dto.CheckEmailDTO;
import com.example.dto.Status;
import com.example.dto.TwoFactorCodeDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
@AllArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;
    private final RefreshTokenService refreshTokenService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final TopicConfig config;
    private final ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;
    private final CustomMetricService customMetricService;
    private final UserSettingRepository userSettingRepository;
    private final UserService userService;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Integer TIMEOUT = 5;

    @Loggable(process = "регистрация пользователя")
    @CacheEvict(value = "users", allEntries = true)
    public ResponseTokenRefreshDTO signUp(RegisterDTO registerDTO) {
        RoleUser role = roleRepository.findByName(Role.STUDENT)
                .orElseThrow(() -> new NotFoundException("Role not found!"));

        User user = createUserEntity(registerDTO, role);

        saveUserInUserRepository(user);

        UserSetting setting = createUserSettingEntity(user);

        saveUserSettingInUserSettingRepository(setting);

        return generateJwtToken(user);
    }

    private User createUserEntity(RegisterDTO registerDTO, RoleUser role) {
        return User.builder()
                .fname(registerDTO.getFirstName())
                .sname(registerDTO.getSecondName())
                .email(registerDTO.getEmail())
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .roleUser(role)
                .build();
    }

    private UserSetting createUserSettingEntity(User user) {
        return UserSetting.builder()
                .user(user)
                .twoFactor(true)
                .build();
    }

    private void saveUserInUserRepository(User user) {
        userRepository.save(user);
    }

    private void saveUserSettingInUserSettingRepository(UserSetting setting) {
        userSettingRepository.save(setting);
    }

    @Loggable(process = "аутентификация пользователя")
    @CacheEvict(value = "users", allEntries = true)
    public Object signIn(LoginDTO loginDTO) {
        authenticate(loginDTO);

        User user = loadUserByUsername(loginDTO.getEmail());

        UserSetting setting = userSettingRepository.findByUser(user);

        if (setting.isTwoFactor()) {
            sendAsyncMessageToKafka(config.getEmailRequest(), loginDTO.getEmail());

            return "A code has been sent to your email!";
        }

        return generateJwtToken(user);
    }

    private void authenticate(LoginDTO loginDTO) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDTO.getEmail(),
                loginDTO.getPassword()
        ));
    }

    private void sendAsyncMessageToKafka(String topic, String message) {
        kafkaTemplate.send(topic, message)
                .thenAcceptAsync(result -> log.info("Sent message: {} with offset {}", message,
                        result.getRecordMetadata().offset()))
                .exceptionallyAsync(error -> {
                    throw new KafkaSendMessageException("Error sending message in Kafka!");
                });
    }

    @Loggable(process = "двухфакторная аутентификация пользователя")
    @Timed
    public ResponseTokenRefreshDTO twoFactorAuthentication(TwoFactorCodeDTO dto) {
        var future = sendSyncMessageToKafka(config.getGetEmail(), dto);
        var response = getResponseFromEmailMicroservice(future);

        CheckEmailDTO emailDTO = deserializeResultData(response.value());

        validateStatus(emailDTO.getStatus());

        User user = loadUserByUsername(emailDTO.getEmail());

        return generateJwtToken(user);
    }

    private RequestReplyFuture<String, String, String> sendSyncMessageToKafka(String topic, TwoFactorCodeDTO dto) {
        String json = serializeTwoFactorDto(dto);

        ProducerRecord<String, String> record = new ProducerRecord<>(topic, json);

        return replyingKafkaTemplate.sendAndReceive(record);
    }

    private ConsumerRecord<String, String> getResponseFromEmailMicroservice(
            RequestReplyFuture<String, String, String> future) {
        try {
            return future.get(TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            customMetricService.incrementKafkaErrorMetric();
            throw new MicroserviceUnavailableException("Microservice email_service is unavailable now!");
        }
    }

    private String serializeTwoFactorDto(TwoFactorCodeDTO dto) {
        try {
            return mapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new JsonParseException("Error json serialize!");
        }
    }

    private void validateStatus(Status status) {
        if (status != Status.OK) {
            throw new TwoFactorAuthenticationException("Email microservice returned non ok status!");
        }
    }

    private CheckEmailDTO deserializeResultData(String json) {
        try {
            return mapper.readValue(json, CheckEmailDTO.class);
        } catch (JsonProcessingException e) {
            throw new JsonParseException("Error parse json from email_service microservice!");
        }
    }

    private ResponseTokenRefreshDTO generateJwtToken(User user) {
        String jwt = jwtTokenProvider.generateToken(user);
        var refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new ResponseTokenRefreshDTO(jwt, refreshToken.getToken());
    }

    private User loadUserByUsername(String email) {
        return (User) userService.loadUserByUsername(email);
    }
}
