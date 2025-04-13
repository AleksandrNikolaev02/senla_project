package com.example.email_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class TopicConfig {
    @Value("${topics.get-email-reply}")
    private String getEmailReply;
}
