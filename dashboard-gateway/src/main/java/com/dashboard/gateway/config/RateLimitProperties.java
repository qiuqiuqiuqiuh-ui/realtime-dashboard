package com.dashboard.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 限流配置 - 支持 Nacos 动态刷新
 *
 * 面试亮点：
 * 1. @RefreshScope：Nacos 配置变更时自动刷新 Bean
 * 2. 无需重启服务即可调整限流阈值
 * 3. 生产环境可配合 Nacos 控制台实时调参
 *
 * 使用方式：
 * 在 Nacos 控制台修改 dashboard-gateway.yml 中的 ratelimit.* 值，
 * 服务会自动刷新配置，下一个请求即生效。
 */
@Component
@RefreshScope
public class RateLimitProperties {

    @Value("${ratelimit.sliding-window.qps:5000}")
    private int slidingWindowQps;

    @Value("${ratelimit.sliding-window.window-ms:1000}")
    private int slidingWindowMs;

    @Value("${ratelimit.sliding-window.sub-window-count:10}")
    private int slidingWindowSubCount;

    @Value("${ratelimit.token-bucket.permits-per-second:6000}")
    private double tokenBucketPermitsPerSecond;

    @Value("${ratelimit.token-bucket.max-permits:12000}")
    private double tokenBucketMaxPermits;

    public int getSlidingWindowQps() { return slidingWindowQps; }
    public int getSlidingWindowMs() { return slidingWindowMs; }
    public int getSlidingWindowSubCount() { return slidingWindowSubCount; }
    public double getTokenBucketPermitsPerSecond() { return tokenBucketPermitsPerSecond; }
    public double getTokenBucketMaxPermits() { return tokenBucketMaxPermits; }
}
