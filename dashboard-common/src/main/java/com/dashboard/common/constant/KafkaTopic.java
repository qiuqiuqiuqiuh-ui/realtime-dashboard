package com.dashboard.common.constant;

/**
 * Kafka Topic 常量
 */
public final class KafkaTopic {
    private KafkaTopic() {}

    /** 原始指标数据 Topic */
    public static final String METRIC_RAW = "dashboard-metric-raw";

    /** 聚合结果 Topic */
    public static final String METRIC_AGGREGATED = "dashboard-metric-aggregated";

    /** 默认分区数 */
    public static final int DEFAULT_PARTITIONS = 3;

    /** 默认副本数 */
    public static final short DEFAULT_REPLICAS = 1;
}
