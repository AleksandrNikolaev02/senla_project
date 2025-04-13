package com.example.file_service.metric;

import com.example.file_service.config.MetricConfig;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class CustomMetricService {
    private final Counter errorMinioServiceMetric;
    private final Counter successMinioServiceMetric;

    public CustomMetricService(MeterRegistry meterRegistry, MetricConfig metricConfig) {
        this.errorMinioServiceMetric = Counter.builder(metricConfig.getErrorMinioServiceMetric())
                .description("Метрика для учета контроля ошибок сервиса Minio")
                .register(meterRegistry);

        this.successMinioServiceMetric = Counter.builder(metricConfig.getSuccessMinioServiceMetric())
                .description("Метрика для учета контроля успешных запросов к Minio")
                .register(meterRegistry);
    }

    public void incrementErrorMinioServiceMetric() {
        errorMinioServiceMetric.increment();
    }

    public void incrementSuccessMinioServiceMetric() {
        successMinioServiceMetric.increment();
    }
}
