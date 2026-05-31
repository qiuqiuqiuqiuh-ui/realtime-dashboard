package com.dashboard.flink.sink;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Kafka 结果输出 (自定义 Sink，不依赖 flink-connector-kafka)
 *
 * 直接使用标准 Kafka Producer 发送消息。
 */
public class KafkaResultSink extends RichSinkFunction<String> {

    private static final Logger log = LoggerFactory.getLogger(KafkaResultSink.class);

    private final String bootstrapServers;
    private final String topic;

    private transient KafkaProducer<String, String> producer;

    public KafkaResultSink(String bootstrapServers, String topic) {
        this.bootstrapServers = bootstrapServers;
        this.topic = topic;
    }

    @Override
    public void open(Configuration parameters) {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("acks", "1");
        props.put("retries", 3);
        producer = new KafkaProducer<>(props);
        log.info("Kafka Sink 初始化完成: servers={}, topic={}", bootstrapServers, topic);
    }

    @Override
    public void invoke(String value, Context context) {
        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, value);
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    log.error("Kafka 发送失败: topic={}", topic, exception);
                }
            });
        } catch (Exception e) {
            log.error("Kafka 发送异常", e);
        }
    }

    @Override
    public void close() {
        if (producer != null) {
            producer.flush();
            producer.close();
            log.info("Kafka Producer 已关闭");
        }
    }
}
