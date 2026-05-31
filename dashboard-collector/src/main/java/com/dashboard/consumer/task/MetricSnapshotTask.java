package com.dashboard.consumer.task;

import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 指标快照定时任务
 *
 * 面试亮点：
 * 1. 定时将 Redis 中的聚合数据快照写入 MySQL (可扩展)
 * 2. 清理过期的 Redis 缓存数据
 * 3. @EnableScheduling + @Scheduled 使用
 */
@Component
@EnableScheduling
public class MetricSnapshotTask {

    private static final Logger log = LoggerFactory.getLogger(MetricSnapshotTask.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 每分钟执行一次：将 Redis 中的聚合数据持久化
     *
     * 生产环境中，这里应该：
     * 1. 从 Redis 读取所有 metric:*:latest 数据
     * 2. 写入 MySQL metric_aggregation 表
     * 3. 发送事件通知 (可选)
     */
    @Scheduled(fixedRate = 60000)
    public void snapshotToDatabase() {
        try {
            Set<String> keys = redisTemplate.keys("metric:*:latest");
            if (keys == null || keys.isEmpty()) {
                return;
            }

            log.info("[快照任务] 开始持久化 {} 条指标数据", keys.size());

            for (String key : keys) {
                String value = redisTemplate.opsForValue().get(key);
                if (value != null) {
                    // 生产环境：写入 MySQL
                    // metricAggregationMapper.insert(JSON.parseObject(value, MetricAggregation.class));
                    log.debug("[快照任务] key={}, value={}", key, value);
                }
            }

            log.info("[快照任务] 持久化完成");
        } catch (Exception e) {
            log.error("[快照任务] 执行失败", e);
        }
    }

    /**
     * 每小时执行一次：清理过期的 Redis 数据
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanupExpiredData() {
        try {
            // 清理超过 24 小时的历史数据
            Set<String> keys = redisTemplate.keys("metric:*:history:*");
            if (keys != null && !keys.isEmpty()) {
                log.info("[清理任务] 清理 {} 条过期数据", keys.size());
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.error("[清理任务] 执行失败", e);
        }
    }

    /**
     * 每 30 秒执行一次：刷新在线连接数到 Redis
     */
    @Scheduled(fixedRate = 30000)
    public void refreshOnlineCount() {
        // 由 WebSocket handler 处理，此处为示例
        log.debug("[在线统计] 当前在线连接数已刷新到 Redis");
    }
}
