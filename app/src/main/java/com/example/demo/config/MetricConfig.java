package com.example.demo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "metrics")
@Getter
@Setter
public class MetricConfig {
    private String errorMetric;
    private String kafkaErrorMetric;
}
