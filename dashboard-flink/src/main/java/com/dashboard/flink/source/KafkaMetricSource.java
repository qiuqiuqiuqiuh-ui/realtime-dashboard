package com.dashboard.flink.source;

import com.alibaba.fastjson2.JSON;
import com.dashboard.common.dto.MetricData;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * Kafka 数据源 (自定义 SourceFunction)
 *
 * 不依赖 flink-connector-kafka (该包在国内仓库下载困难)，
 * 直接使用标准 Kafka Client 手动拉取消息。
 *
 * 面试亮点：
 * 1. 自定义 RichSourceFunction 可访问 Flink 上下文
 * 2. 手动管理 Kafka Consumer 生命周期 (open / run / close)
 * 3. 可配置 offset、group、超时等参数
 */
public class KafkaMetricSource extends RichSourceFunction<MetricData> {

    private static final Logger log = LoggerFactory.getLogger(KafkaMetricSource.class);

    private final String bootstrapServers;
    private final String topic;
    private final String groupId;

    private transient KafkaConsumer<String, String> consumer;
    private volatile boolean running = true;

    public KafkaMetricSource(String bootstrapServers, String topic, String groupId) {
        this.bootstrapServers = bootstrapServers;
        this.topic = topic;
        this.groupId = groupId;
    }

    /**
     * 初始化 Kafka Consumer (在 Task 线程中执行)
     */
    @Override
    public void open(org.apache.flink.configuration.Configuration parameters) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));
        log.info("Kafka Source 初始化完成: servers={}, topic={}, group={}", bootstrapServers, topic, groupId);
    }

    /**
     * 持续拉取消息并发射到 Flink 流
     */
    @Override
    public void run(SourceContext<MetricData> ctx) throws Exception {
        while (running) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
            for (ConsumerRecord<String, String> record : records) {
                try {
                    MetricData data = JSON.parseObject(record.value(), MetricData.class);
                    if (data != null && data.getMetricName() != null) {
                        ctx.collect(data);
                    }
                } catch (Exception e) {
                    log.warn("消息解析失败: topic={}, offset={}", record.topic(), record.offset(), e);
                }
            }
        }
    }

    /**
     * 关闭 Kafka Consumer
     */
    @Override
    public void cancel() {
        running = false;
        if (consumer != null) {
            consumer.close();
            log.info("Kafka Consumer 已关闭");
        }
    }

    /**
     * 创建数据流 (便捷方法)
     */
    public DataStream<MetricData> buildStream(StreamExecutionEnvironment env) {
        return env.addSource(this)
                .name("kafka-custom-source")
                .returns(TypeInformation.of(MetricData.class));
    }
}
