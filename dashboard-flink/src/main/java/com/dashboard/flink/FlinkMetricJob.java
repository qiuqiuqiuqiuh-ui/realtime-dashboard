package com.dashboard.flink;

import com.alibaba.fastjson2.JSON;
import com.dashboard.common.dto.AggregatedData;
import com.dashboard.common.dto.MetricData;
import com.dashboard.flink.function.MetricAggregateFunction;
import com.dashboard.flink.function.MetricWindowResultProcess;
import com.dashboard.flink.sink.KafkaResultSink;
import com.dashboard.flink.sink.RedisResultSink;
import com.dashboard.flink.source.KafkaMetricSource;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Flink 实时流计算 Job
 *
 * 核心链路：
 * Kafka(原始指标) → 数据清洗 → 窗口聚合 → 双路输出(Kafka + Redis)
 *
 * 面试亮点：
 * 1. 滑动窗口聚合 (5秒窗口, 1秒滑动)
 * 2. EventTime + Watermark 处理乱序数据
 * 3. allowedLateness 允许迟到数据
 * 4. 增量聚合 AggregateFunction (高性能)
 * 5. 双路输出：Kafka (供 WebSocket 推送) + Redis (低延迟缓存)
 * 6. Checkpoint 保证 Exactly-Once 语义
 */
public class FlinkMetricJob {

    private static final Logger log = LoggerFactory.getLogger(FlinkMetricJob.class);

    // 配置项 (生产环境从 Nacos 读取)
    private static final String KAFKA_BOOTSTRAP_SERVERS = System.getenv().getOrDefault("KAFKA_SERVERS", "localhost:9092");
    private static final String KAFKA_SOURCE_TOPIC = "dashboard-metric-raw";
    private static final String KAFKA_SINK_TOPIC = "dashboard-metric-aggregated";
    private static final String KAFKA_GROUP_ID = "flink-metric-group";
    private static final String REDIS_HOST = System.getenv().getOrDefault("REDIS_HOST", "localhost");
    private static final int REDIS_PORT = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));

    // 窗口参数
    private static final int WINDOW_SIZE_SECONDS = 5;
    private static final int WINDOW_SLIDE_SECONDS = 1;
    private static final int ALLOWED_LATENESS_SECONDS = 3;

    public static void main(String[] args) throws Exception {
        log.info("========== Flink Metric Job 启动 ==========");
        log.info("Kafka: {}, Topic: {}", KAFKA_BOOTSTRAP_SERVERS, KAFKA_SOURCE_TOPIC);
        log.info("Redis: {}:{}", REDIS_HOST, REDIS_PORT);

        // 1. 创建执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 2. Checkpoint 配置
        env.enableCheckpointing(10000);
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(5000);
        env.getCheckpointConfig().setCheckpointTimeout(60000);

        // 3. 自定义 Kafka Source
        DataStream<MetricData> metricStream = env
                .addSource(new KafkaMetricSource(KAFKA_BOOTSTRAP_SERVERS, KAFKA_SOURCE_TOPIC, KAFKA_GROUP_ID))
                .name("kafka-source");

        // 4. 数据清洗
        DataStream<MetricData> cleanedStream = metricStream
                .filter(data -> data != null
                        && data.getMetricName() != null
                        && !data.getMetricName().isEmpty()
                        && data.getTimestamp() > 0)
                .name("filter-invalid");

        // 5. 按指标名分组 + 滑动窗口聚合
        SingleOutputStreamOperator<AggregatedData> aggregatedStream = cleanedStream
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<MetricData>forMonotonousTimestamps()
                                .withTimestampAssigner((event, timestamp) -> event.getTimestamp())
                )
                .keyBy(MetricData::getMetricName)
                .window(SlidingEventTimeWindows.of(
                        Time.seconds(WINDOW_SIZE_SECONDS),
                        Time.seconds(WINDOW_SLIDE_SECONDS)))
                .allowedLateness(Time.seconds(ALLOWED_LATENESS_SECONDS))
                .aggregate(
                        new MetricAggregateFunction(),
                        new MetricWindowResultProcess()
                )
                .name("window-aggregate");

        // 6. 双路输出
        // 路径 A: 写回 Kafka (自定义 Sink)
        aggregatedStream
                .map(data -> JSON.toJSONString(data))
                .returns(String.class)
                .addSink(new KafkaResultSink(KAFKA_BOOTSTRAP_SERVERS, KAFKA_SINK_TOPIC))
                .name("kafka-sink");

        // 路径 B: 直写 Redis
        aggregatedStream
                .addSink(new RedisResultSink(REDIS_HOST, REDIS_PORT))
                .name("redis-sink");

        // 7. 启动
        log.info("Flink Job 开始执行...");
        env.execute("Dashboard Flink Metric Job");
    }
}
