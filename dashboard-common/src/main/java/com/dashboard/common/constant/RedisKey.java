package com.dashboard.common.constant;

/**
 * Redis Key 常量
 */
public final class RedisKey {
    private RedisKey() {}

    /** 指标聚合结果: metric:{metricName}:latest */
    public static final String METRIC_LATEST = "metric:%s:latest";

    /** 指标排行榜: metric:{metricName}:ranking */
    public static final String METRIC_RANKING = "metric:%s:ranking";

    /** 滑动窗口限流: rate_limit:{path}:{window} */
    public static final String RATE_LIMIT = "rate_limit:%s:%d";

    /** 在线连接数 */
    public static final String WS_ONLINE_COUNT = "ws:online:count";
}
