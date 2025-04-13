package com.example.file_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "topics")
@Configuration
public class TopicConfig {
    private String getFileReply;
    private String fileResponses;
    private String fileAnswers;

    public String getGetFileReply() {
        return getFileReply;
    }

    public void setGetFileReply(String getFileReply) {
        this.getFileReply = getFileReply;
    }

    public String getFileResponses() {
        return fileResponses;
    }

    public void setFileResponses(String fileResponses) {
        this.fileResponses = fileResponses;
    }

    public String getFileAnswers() {
        return fileAnswers;
    }

    public void setFileAnswers(String fileAnswers) {
        this.fileAnswers = fileAnswers;
    }
}
