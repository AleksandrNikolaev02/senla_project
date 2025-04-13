package com.example.demo.config;

import com.example.demo.service.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.spec.SecretKeySpec;

@Configuration
public class JwtConfig {
    @Value("${jwt.secret_key}")
    private String secretKey;

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKeySpec jwtSecretKey = new SecretKeySpec(secretKey.getBytes(), "");
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    public JwtTokenProvider jwtTokenProvider(JwtDecoder jwtDecoder) {
        return new JwtTokenProvider(jwtDecoder);
    }
}

