package com.dashboard.common.dto;

import java.io.Serializable;

/**
 * WebSocket 推送消息格式
 */
public class DashboardMessage<T> implements Serializable {
    private String type;     // 消息类型: metric, alert, system
    private T data;          // 消息数据
    private long timestamp;  // 推送时间

    public DashboardMessage() {}

    public DashboardMessage(String type, T data) {
        this.type = type;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> DashboardMessage<T> of(String type, T data) {
        return new DashboardMessage<>(type, data);
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
