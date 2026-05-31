package com.dashboard.storage.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 指标数据实体 - 对应 metric_data 表
 */
@TableName("metric_data")
public class MetricDataEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("metric_name")
    private String metricName;

    @TableField("value")
    private Double value;

    @TableField("tags")
    private String tags;  // JSON 格式存储

    @TableField("source")
    private String source;  // 数据来源

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
