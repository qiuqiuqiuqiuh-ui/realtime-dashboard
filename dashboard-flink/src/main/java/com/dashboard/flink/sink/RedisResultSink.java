package com.dashboard.flink.sink;

import com.alibaba.fastjson2.JSON;
import com.dashboard.common.dto.AggregatedData;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Redis 直写 Sink
 *
 * 将聚合结果直接写入 Redis，跳过 Kafka，降低延迟。
 *
 * 面试亮点：
 * 1. RichSinkFunction 生命周期管理 (open/close)
 * 2. JedisPool 连接池复用，避免频繁创建连接
 * 3. 双写：Redis 缓存 + 持久化 Key
 * 4. TTL 过期策略，自动清理过期数据
 */
public class RedisResultSink extends RichSinkFunction<AggregatedData> {

    private static final Logger log = LoggerFactory.getLogger(RedisResultSink.class);

    private final String host;
    private final int port;

    private transient JedisPool jedisPool;

    public RedisResultSink(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(10);
        config.setMaxIdle(5);
        config.setMinIdle(1);
        config.setTestOnBorrow(true);
        this.jedisPool = new JedisPool(config, host, port);
        log.info("Redis Sink 初始化完成: {}:{}", host, port);
    }

    @Override
    public void invoke(AggregatedData data, Context context) throws Exception {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = "metric:" + data.getMetricName() + ":latest";
            String value = JSON.toJSONString(data);

            // 写入最新聚合结果，TTL 60 秒
            jedis.setex(key, 60, value);

            // 写入历史列表 (保留最近 100 条)
            String historyKey = "metric:" + data.getMetricName() + ":history";
            jedis.lpush(historyKey, value);
            jedis.ltrim(historyKey, 0, 99);

            log.debug("Redis 写入成功: key={}", key);
        } catch (Exception e) {
            log.error("Redis 写入失败: metric={}", data.getMetricName(), e);
        }
    }

    @Override
    public void close() throws Exception {
        if (jedisPool != null) {
            jedisPool.close();
        }
        super.close();
    }
}
