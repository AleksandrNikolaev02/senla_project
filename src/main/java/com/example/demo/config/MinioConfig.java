package com.example.demo.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {
    @Value("${minio.server.url}")
    private String url;
    @Value("${minio.server.access_key}")
    private String accessKey;
    @Value("${minio.server.secret_key}")
    private String secretKey;
    @Value("${minio.server.port}")
    private Integer port;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(url, port, false)
                .credentials(accessKey, secretKey)
                .build();
    }
}
