package com.dashboard.gateway.ratelimit;

/**
 * 令牌桶限流器
 *
 * 与滑动窗口的区别：
 * - 滑动窗口：严格限制 QPS，不允许突发
 * - 令牌桶：允许一定程度的突发流量 (桶内预存令牌)
 *
 * 面试亮点：
 * 1. 令牌生成速率恒定 (permitsPerSecond)
 * 2. 桶满时多余的令牌丢弃 (防积压)
 * 3. 允许瞬间消费桶内所有令牌 (突发流量)
 * 4. synchronized 保证线程安全
 */
public class TokenBucketRateLimiter {

    private final double permitsPerSecond;  // 每秒生成的令牌数
    private final double maxPermits;        // 桶的最大容量
    private double storedPermits;           // 当前桶内令牌数
    private long nextFreeTimeMicros;        // 下一个令牌生成时间 (微秒)

    public TokenBucketRateLimiter(double permitsPerSecond) {
        this(permitsPerSecond, permitsPerSecond * 2);  // 默认桶容量 = 2 秒的量
    }

    public TokenBucketRateLimiter(double permitsPerSecond, double maxPermits) {
        this.permitsPerSecond = permitsPerSecond;
        this.maxPermits = maxPermits;
        this.storedPermits = 0;
        this.nextFreeTimeMicros = nowMicros();
    }

    /**
     * 尝试获取 1 个令牌
     * @return true: 获取成功, false: 桶内无令牌
     */
    public boolean tryAcquire() {
        return tryAcquire(1);
    }

    /**
     * 尝试获取 N 个令牌
     * @param permits 需要的令牌数
     * @return true: 获取成功
     */
    public synchronized boolean tryAcquire(int permits) {
        long nowMicros = nowMicros();

        // 1. 补充令牌：根据流逝时间生成新令牌
        if (nowMicros > nextFreeTimeMicros) {
            double newPermits = (nowMicros - nextFreeTimeMicros) / 1_000_000.0 * permitsPerSecond;
            storedPermits = Math.min(maxPermits, storedPermits + newPermits);
            nextFreeTimeMicros = nowMicros;
        }

        // 2. 尝试消费令牌
        if (storedPermits >= permits) {
            storedPermits -= permits;
            return true;
        }

        return false;
    }

    /**
     * 预热：系统启动时桶内预存令牌，应对冷启动突发
     */
    public void warmUp(double initialPermits) {
        this.storedPermits = Math.min(maxPermits, initialPermits);
    }

    private long nowMicros() {
        return System.nanoTime() / 1_000;
    }

    public double getPermitsPerSecond() { return permitsPerSecond; }
    public double getStoredPermits() { return storedPermits; }
    public double getMaxPermits() { return maxPermits; }
}
