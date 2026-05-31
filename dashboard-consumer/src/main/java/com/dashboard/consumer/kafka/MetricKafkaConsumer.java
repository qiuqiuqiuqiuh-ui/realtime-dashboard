package com.dashboard.consumer.kafka;

import com.alibaba.fastjson2.JSON;
import com.dashboard.common.constant.KafkaTopic;
import com.dashboard.common.dto.AggregatedData;
import com.dashboard.common.dto.DashboardMessage;
import com.dashboard.common.dto.MetricData;
import com.dashboard.consumer.netty.WebSocketChannelManager;
import com.dashboard.consumer.service.MetricAggregationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Kafka 消费者 - 消费原始指标数据
 *
 * 面试亮点：
 * 1. 手动 ACK，处理成功才确认
 * 2. 多线程消费 (配置 concurrency = 3)
 * 3. 实时聚合 + Redis 缓存 + Netty WebSocket 推送
 */
@Component
public class MetricKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(MetricKafkaConsumer.class);

    @Autowired
    private MetricAggregationService aggregationService;

    @Autowired
    private WebSocketChannelManager channelManager;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @KafkaListener(topics = KafkaTopic.METRIC_RAW, containerFactory = "kafkaListenerContainerFactory")
    public void onMessage(ConsumerRecord<String, MetricData> record, Acknowledgment ack) {
        try {
            MetricData data = record.value();
            log.debug("收到消息: topic={}, partition={}, offset={}, data={}",
                    record.topic(), record.partition(), record.offset(), data);

            // 1. 实时聚合计算
            AggregatedData aggregated = aggregationService.aggregate(data);

            // 2. 存入 Redis
            String redisKey = "metric:" + data.getMetricName() + ":latest";
            redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(aggregated));

            // 3. 通过 Netty WebSocket 推送
            DashboardMessage<AggregatedData> message = DashboardMessage.of("metric", aggregated);
            channelManager.broadcast(JSON.toJSONString(message));

            // 4. 手动确认
            ack.acknowledge();

        } catch (Exception e) {
            log.error("消费消息失败: topic={}, offset={}", record.topic(), record.offset(), e);
        }
    }
}
