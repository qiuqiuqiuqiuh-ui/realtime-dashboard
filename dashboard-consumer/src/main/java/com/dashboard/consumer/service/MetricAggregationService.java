package com.dashboard.consumer.service;

import com.dashboard.common.dto.AggregatedData;
import com.dashboard.common.dto.MetricData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 指标聚合服务
 *
 * 面试亮点：
 * 1. 滑动窗口聚合 (5 秒一个窗口)
 * 2. ConcurrentHashMap 保证线程安全
 * 3. 窗口自动过期清理
 */
@Service
public class MetricAggregationService {

    private static final Logger log = LoggerFactory.getLogger(MetricAggregationService.class);

    /** 聚合窗口大小 (毫秒) */
    private static final long WINDOW_SIZE_MS = 5000;

    // key: metricName + windowStart, value: 聚合数据
    private final ConcurrentHashMap<String, AggregatedData> windows = new ConcurrentHashMap<>();

    /**
     * 对单条数据进行实时聚合
     */
    public AggregatedData aggregate(MetricData data) {
        long windowStart = (data.getTimestamp() / WINDOW_SIZE_MS) * WINDOW_SIZE_MS;
        long windowEnd = windowStart + WINDOW_SIZE_MS;
        String key = data.getMetricName() + ":" + windowStart;

        AggregatedData aggregated = windows.compute(key, (k, existing) -> {
            if (existing == null) {
                AggregatedData agg = new AggregatedData(data.getMetricName(), windowStart, windowEnd);
                agg.accumulate(data.getValue());
                return agg;
            } else {
                existing.accumulate(data.getValue());
                return existing;
            }
        });

        // 异步清理过期窗口
        cleanExpiredWindows();

        return aggregated;
    }

    /**
     * 清理过期的聚合窗口
     */
    private void cleanExpiredWindows() {
        long now = System.currentTimeMillis();
        windows.entrySet().removeIf(entry -> {
            long windowEnd = entry.getValue().getWindowEnd();
            return now - windowEnd > WINDOW_SIZE_MS * 2;  // 保留 2 个窗口周期
        });
    }

    /**
     * 获取指定指标的最新聚合数据
     */
    public AggregatedData getLatest(String metricName) {
        return windows.values().stream()
                .filter(a -> a.getMetricName().equals(metricName))
                .max((a, b) -> Long.compare(a.getWindowEnd(), b.getWindowEnd()))
                .orElse(null);
    }
}
