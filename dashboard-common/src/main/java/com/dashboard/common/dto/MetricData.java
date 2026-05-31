package com.dashboard.common.dto;

import java.io.Serializable;
import java.util.Map;

/**
 * 指标数据 - 数据生产者上报的数据格式
 */
public class MetricData implements Serializable {
    private String metricName;           // 指标名称 (如 qps, rt, cpu)
    private double value;                // 指标值
    private Map<String, String> tags;    // 标签 (region=cn, type=http)
    private long timestamp;              // 数据产生时间戳

    public MetricData() {}

    public MetricData(String metricName, double value, Map<String, String> tags, long timestamp) {
        this.metricName = metricName;
        this.value = value;
        this.tags = tags;
        this.timestamp = timestamp;
    }

    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
    public Map<String, String> getTags() { return tags; }
    public void setTags(Map<String, String> tags) { this.tags = tags; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "MetricData{metricName='" + metricName + "', value=" + value + ", tags=" + tags + ", timestamp=" + timestamp + "}";
    }
}
