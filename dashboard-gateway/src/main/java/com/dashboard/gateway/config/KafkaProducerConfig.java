package com.dashboard.gateway.config;

import com.dashboard.common.dto.MetricData;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka 生产者配置
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, MetricData> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // 高吞吐配置
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, 65536);           // 批量大小 64KB (原 16KB)
        config.put(ProducerConfig.LINGER_MS_CONFIG, 10);               // 等待 10ms 凑批 (原 5ms)
        config.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 67108864);     // 缓冲区 64MB (原 32MB)
        config.put(ProducerConfig.ACKS_CONFIG, "1");                   // Leader 确认
        config.put(ProducerConfig.RETRIES_CONFIG, 3);                  // 重试 3 次
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");     // 压缩 (减少网络传输)
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, MetricData> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
