package com.example.demo.metric;

import com.example.demo.config.MetricConfig;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class CustomMetricService {
    private final Counter errorMetric;
    private final Counter kafkaErrorMetric;

    public CustomMetricService(MeterRegistry meterRegistry, MetricConfig metricConfig) {
        errorMetric = Counter.builder(metricConfig.getErrorMetric())
                .description("Кол-во ошибок в приложении (статус 5хх)")
                .register(meterRegistry);

        kafkaErrorMetric = Counter.builder(metricConfig.getKafkaErrorMetric())
                .description("Кол-во неудачных запросов в Kafka")
                .register(meterRegistry);
    }

    public void incrementErrorMetric() {
        errorMetric.increment();
    }

    public void incrementKafkaErrorMetric() {
        kafkaErrorMetric.increment();
    }
}
