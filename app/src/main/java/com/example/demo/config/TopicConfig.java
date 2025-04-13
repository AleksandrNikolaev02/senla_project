package com.example.demo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "topics")
@Getter
@Setter
public class TopicConfig {
    private String fileEvents;
    private String getFileRequest;
    private String deleteFileRequest;
    private String emailRequest;
    private String getEmail;
}
