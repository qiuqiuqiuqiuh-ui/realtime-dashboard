package com.dashboard.gateway.ratelimit;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 滑动窗口限流器
 *
 * 原理：将时间窗口划分为多个小格子，每个格子独立计数，
 * 通过滑动窗口统计当前窗口内的总请求数。
 *
 * 面试亮点：滑动窗口 vs 固定窗口的区别，解决临界突刺问题。
 */
public class SlidingWindowRateLimiter {

    private final int maxQps;           // 最大 QPS
    private final int windowSizeMs;     // 窗口大小 (毫秒)
    private final int subWindowCount;   // 子窗口数量
    private final int subWindowSizeMs;  // 子窗口大小

    // key: 子窗口起始时间戳, value: 该子窗口的请求计数
    private final ConcurrentHashMap<Long, AtomicInteger> subWindows = new ConcurrentHashMap<>();

    public SlidingWindowRateLimiter(int maxQps) {
        this(maxQps, 1000, 10); // 默认 1 秒窗口，10 个子窗口
    }

    public SlidingWindowRateLimiter(int maxQps, int windowSizeMs, int subWindowCount) {
        this.maxQps = maxQps;
        this.windowSizeMs = windowSizeMs;
        this.subWindowCount = subWindowCount;
        this.subWindowSizeMs = windowSizeMs / subWindowCount;
    }

    /**
     * 尝试获取令牌
     * @return true: 允许通过, false: 限流
     */
    public boolean tryAcquire() {
        long now = System.currentTimeMillis();
        long currentSubWindowKey = now / subWindowSizeMs * subWindowSizeMs;

        // 清理过期子窗口
        cleanExpiredSubWindows(currentSubWindowKey);

        // 统计当前窗口内的总请求数
        int totalCount = countCurrentWindow(currentSubWindowKey);

        if (totalCount >= maxQps) {
            return false;
        }

        // 原子递增当前子窗口计数
        subWindows.computeIfAbsent(currentSubWindowKey, k -> new AtomicInteger(0)).incrementAndGet();
        return true;
    }

    private void cleanExpiredSubWindows(long currentKey) {
        long windowStart = currentKey - windowSizeMs + subWindowSizeMs;
        subWindows.keySet().removeIf(key -> key < windowStart);
    }

    private int countCurrentWindow(long currentKey) {
        long windowStart = currentKey - windowSizeMs + subWindowSizeMs;
        return subWindows.entrySet().stream()
                .filter(e -> e.getKey() >= windowStart && e.getKey() <= currentKey)
                .mapToInt(e -> e.getValue().get())
                .sum();
    }

    public int getMaxQps() { return maxQps; }
}
