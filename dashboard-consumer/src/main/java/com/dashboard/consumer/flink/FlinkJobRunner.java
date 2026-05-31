package com.dashboard.consumer.flink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Flink 任务运行器
 *
 * 注意：Flink 嵌入 Spring Boot 有类加载冲突问题 (flink-core 与 Spring Boot classpath 不兼容)。
 *
 * 生产环境方案：Flink 独立部署 (flink run -c ...)
 * 演示环境方案：用 Spring Kafka Consumer + 内存聚合替代
 *
 * 本类仅做日志提示，实际聚合逻辑由 MetricAggregationService 处理。
 */
@Component
public class FlinkJobRunner {

    private static final Logger log = LoggerFactory.getLogger(FlinkJobRunner.class);

    @jakarta.annotation.PostConstruct
    public void start() {
        log.info("Flink 嵌入模式不可用 (类加载冲突)，使用 Spring 内存聚合替代");
        log.info("生产环境请部署独立 Flink 集群: flink run -c com.dashboard.flink.FlinkMetricJob dashboard-flink.jar");
    }
}
