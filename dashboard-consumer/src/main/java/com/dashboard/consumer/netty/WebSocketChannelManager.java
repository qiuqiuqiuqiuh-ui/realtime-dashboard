package com.dashboard.consumer.netty;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebSocket 连接管理器 (Netty 版)
 *
 * 面试亮点：
 * 1. ConcurrentHashMap 管理 Channel (线程安全)
 * 2. 原子计数器统计在线人数
 * 3. 分组推送支持 (频道订阅)
 * 4. 定时清理心跳超时的 Channel
 */
@Component
public class WebSocketChannelManager {

    private static final Logger log = LoggerFactory.getLogger(WebSocketChannelManager.class);

    // channelId -> Channel
    private final ConcurrentHashMap<String, Channel> channels = new ConcurrentHashMap<>();

    // channelId -> 最后心跳时间
    private final ConcurrentHashMap<String, Long> heartbeatMap = new ConcurrentHashMap<>();

    // channelName -> channelIds (频道订阅)
    private final ConcurrentHashMap<String, Set<String>> subscriptions = new ConcurrentHashMap<>();

    // 在线计数
    private final AtomicInteger onlineCount = new AtomicInteger(0);

    // 心跳检测定时器
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    private StringRedisTemplate redisTemplate;

    public WebSocketChannelManager() {
        // 每 30 秒清理心跳超时的 Channel
        scheduler.scheduleAtFixedRate(this::cleanTimeoutChannels, 30, 30, TimeUnit.SECONDS);
    }

    /**
     * 添加连接
     */
    public void addChannel(String channelId, Channel channel) {
        channels.put(channelId, channel);
        heartbeatMap.put(channelId, System.currentTimeMillis());
        int count = onlineCount.incrementAndGet();

        // 同步到 Redis
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set("ws:online:count", String.valueOf(count));
        }
    }

    /**
     * 移除连接
     */
    public void removeChannel(String channelId) {
        channels.remove(channelId);
        heartbeatMap.remove(channelId);

        // 从所有频道移除
        subscriptions.values().forEach(set -> set.remove(channelId));

        int count = onlineCount.decrementAndGet();
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set("ws:online:count", String.valueOf(count));
        }
    }

    /**
     * 更新心跳时间
     */
    public void updateHeartbeat(String channelId) {
        heartbeatMap.put(channelId, System.currentTimeMillis());
    }

    /**
     * 订阅频道
     */
    public void subscribe(String channelId, String channelName) {
        subscriptions.computeIfAbsent(channelName, k -> ConcurrentHashMap.newKeySet()).add(channelId);
        log.info("频道订阅: channelId={}, channel={}", channelId, channelName);
    }

    /**
     * 广播消息到所有连接
     */
    public void broadcast(String message) {
        TextWebSocketFrame frame = new TextWebSocketFrame(message);
        channels.forEach((id, channel) -> {
            if (channel.isActive()) {
                channel.writeAndFlush(frame);
            }
        });
    }

    /**
     * 向指定频道推送消息
     */
    public void sendToChannel(String channelName, String message) {
        Set<String> channelIds = subscriptions.get(channelName);
        if (channelIds == null) return;

        TextWebSocketFrame frame = new TextWebSocketFrame(message);
        channelIds.forEach(id -> {
            Channel channel = channels.get(id);
            if (channel != null && channel.isActive()) {
                channel.writeAndFlush(frame);
            }
        });
    }

    /**
     * 清理心跳超时的 Channel (60 秒无心跳)
     */
    private void cleanTimeoutChannels() {
        long now = System.currentTimeMillis();
        long timeout = 60_000;

        heartbeatMap.forEach((channelId, lastTime) -> {
            if (now - lastTime > timeout) {
                Channel channel = channels.get(channelId);
                if (channel != null && channel.isActive()) {
                    log.warn("心跳超时，关闭连接: channelId={}", channelId);
                    channel.close();
                }
                removeChannel(channelId);
            }
        });
    }

    public int getOnlineCount() {
        return onlineCount.get();
    }

    public Map<String, Channel> getChannels() {
        return channels;
    }
}
