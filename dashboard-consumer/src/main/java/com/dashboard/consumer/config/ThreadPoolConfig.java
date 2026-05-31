package com.dashboard.consumer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * 线程池配置
 *
 * 面试亮点：
 * 1. IO 密集型 vs CPU 密集型线程池分离
 * 2. 自定义线程工厂 (命名线程，方便排查)
 * 3. 自定义拒绝策略
 */
@Configuration
public class ThreadPoolConfig {

    /**
     * IO 密集型线程池 - 用于 WebSocket 推送
     * 核心线程 = CPU 核心数 * 2
     */
    @Bean("ioThreadPool")
    public ExecutorService ioThreadPool() {
        int coreSize = Runtime.getRuntime().availableProcessors() * 2;
        return new ThreadPoolExecutor(
                coreSize,                          // 核心线程
                coreSize * 2,                      // 最大线程
                60L, TimeUnit.SECONDS,             // 空闲线程存活时间
                new LinkedBlockingQueue<>(1000),   // 任务队列
                new NamedThreadFactory("io-pool"),  // 线程工厂
                new ThreadPoolExecutor.CallerRunsPolicy()  // 拒绝策略
        );
    }

    /**
     * CPU 密集型线程池 - 用于聚合计算
     * 核心线程 = CPU 核心数 + 1
     */
    @Bean("cpuThreadPool")
    public ExecutorService cpuThreadPool() {
        int coreSize = Runtime.getRuntime().availableProcessors() + 1;
        return new ThreadPoolExecutor(
                coreSize,
                coreSize,
                0L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(500),
                new NamedThreadFactory("cpu-pool"),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    /**
     * 自定义线程工厂 - 给线程命名，方便线程 dump 排查
     */
    static class NamedThreadFactory implements ThreadFactory {
        private final String namePrefix;
        private int counter = 0;

        NamedThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + "-" + (++counter));
            t.setDaemon(false);
            return t;
        }
    }
}
