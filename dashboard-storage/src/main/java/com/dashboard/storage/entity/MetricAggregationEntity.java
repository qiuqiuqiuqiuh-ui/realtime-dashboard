package com.dashboard.storage.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 聚合数据实体 - 对应 metric_aggregation 表
 */
@TableName("metric_aggregation")
public class MetricAggregationEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("metric_name")
    private String metricName;

    @TableField("avg_val")
    private Double avgVal;

    @TableField("max_val")
    private Double maxVal;

    @TableField("min_val")
    private Double minVal;

    @TableField("sum_val")
    private Double sumVal;

    @TableField("count_val")
    private Long countVal;

    @TableField("window_start")
    private LocalDateTime windowStart;

    @TableField("window_end")
    private LocalDateTime windowEnd;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }
    public Double getAvgVal() { return avgVal; }
    public void setAvgVal(Double avgVal) { this.avgVal = avgVal; }
    public Double getMaxVal() { return maxVal; }
    public void setMaxVal(Double maxVal) { this.maxVal = maxVal; }
    public Double getMinVal() { return minVal; }
    public void setMinVal(Double minVal) { this.minVal = minVal; }
    public Double getSumVal() { return sumVal; }
    public void setSumVal(Double sumVal) { this.sumVal = sumVal; }
    public Long getCountVal() { return countVal; }
    public void setCountVal(Long countVal) { this.countVal = countVal; }
    public LocalDateTime getWindowStart() { return windowStart; }
    public void setWindowStart(LocalDateTime windowStart) { this.windowStart = windowStart; }
    public LocalDateTime getWindowEnd() { return windowEnd; }
    public void setWindowEnd(LocalDateTime windowEnd) { this.windowEnd = windowEnd; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
