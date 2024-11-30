package com.example.demo.util;

import com.example.demo.service.JwtTokenProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class Util {
    private JwtTokenProvider jwtTokenProvider;

    public Integer getIdFromToken(String token) {
        Integer user_id = jwtTokenProvider.getIdFromToken(token.substring(7));
        log.info("Процесс: получение id пользователя из токена. Токен: {}, ID: {}", token, user_id);
        return user_id;
    }
}
