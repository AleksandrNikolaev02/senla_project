package com.example.demo.service;

import com.example.demo.annoration.Loggable;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.TokenRefreshException;
import com.example.demo.model.RefreshToken;
import com.example.demo.model.User;
import com.example.demo.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    @Value("${jwt.time-to-live-refresh-token}")
    private Integer timeToLiveRefreshToken;
    private final EntityFindService entityFindService;

    public RefreshToken findByRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Refresh token not found!"));
    }

    @Transactional
    @Loggable(process = "создание refresh токена")
    public RefreshToken createRefreshToken(Integer userId) {
        User user = entityFindService.findUserById(userId);

        if (user.getToken() != null) {
            if (!checkExpiration(user.getToken())) {
                return user.getToken();
            }

            refreshTokenRepository.delete(user.getToken());
        }

        RefreshToken refreshToken = createRefreshTokenEntityFromUser(user);
        saveRefreshTokenResult(user, refreshToken);
        return refreshToken;
    }

    private RefreshToken createRefreshTokenEntityFromUser(User user) {
        return RefreshToken.builder()
                .user(user)
                .expiryDate(LocalDateTime.now().plusSeconds(timeToLiveRefreshToken))
                .token(UUID.randomUUID().toString())
                .build();
    }

    private void saveRefreshTokenResult(User user, RefreshToken refreshToken) {
        user.setToken(refreshToken);
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void verifyExpiration(RefreshToken token) {
        if (checkExpiration(token)) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException("Refresh token was expired!");
        }
    }

    private boolean checkExpiration(RefreshToken token) {
        return token.getExpiryDate().compareTo(LocalDateTime.now()) < 0;
    }
}
