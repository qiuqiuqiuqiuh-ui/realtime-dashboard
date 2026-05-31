-- =============================================
-- 实时数据大屏 - 数据库初始化脚本
-- =============================================

CREATE DATABASE IF NOT EXISTS dashboard DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE dashboard;

-- 指标原始数据表
CREATE TABLE IF NOT EXISTS metric_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    metric_name VARCHAR(64) NOT NULL COMMENT '指标名称',
    value DOUBLE NOT NULL COMMENT '指标值',
    tags JSON COMMENT '标签 (JSON 格式)',
    source VARCHAR(32) DEFAULT 'api' COMMENT '数据来源',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_metric_name (metric_name),
    INDEX idx_created_at (created_at),
    INDEX idx_metric_time (metric_name, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指标原始数据表';

-- 聚合结果表
CREATE TABLE IF NOT EXISTS metric_aggregation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    metric_name VARCHAR(64) NOT NULL COMMENT '指标名称',
    avg_val DOUBLE COMMENT '平均值',
    max_val DOUBLE COMMENT '最大值',
    min_val DOUBLE COMMENT '最小值',
    sum_val DOUBLE COMMENT '总和',
    count_val BIGINT COMMENT '计数',
    window_start DATETIME NOT NULL COMMENT '聚合窗口开始时间',
    window_end DATETIME NOT NULL COMMENT '聚合窗口结束时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_metric_name (metric_name),
    INDEX idx_window (metric_name, window_start)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指标聚合结果表';

-- 告警规则表
CREATE TABLE IF NOT EXISTS alert_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    metric_name VARCHAR(64) NOT NULL COMMENT '指标名称',
    condition_type VARCHAR(16) NOT NULL COMMENT '条件类型: gt, lt, eq, between',
    threshold DOUBLE NOT NULL COMMENT '阈值',
    threshold_max DOUBLE COMMENT '最大阈值 (between 类型)',
    duration INT DEFAULT 60 COMMENT '持续时间 (秒)',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警规则表';

-- 告警记录表
CREATE TABLE IF NOT EXISTS alert_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    rule_id BIGINT NOT NULL COMMENT '规则 ID',
    metric_name VARCHAR(64) NOT NULL COMMENT '指标名称',
    current_value DOUBLE COMMENT '触发时的值',
    threshold DOUBLE COMMENT '阈值',
    status VARCHAR(16) DEFAULT 'firing' COMMENT '状态: firing, resolved',
    fired_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '触发时间',
    resolved_at DATETIME COMMENT '恢复时间',
    INDEX idx_status (status),
    INDEX idx_metric (metric_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警记录表';

-- 插入示例告警规则
INSERT INTO alert_rule (metric_name, condition_type, threshold, duration) VALUES
('qps', 'gt', 5000, 30),
('rt', 'gt', 200, 60),
('error_rate', 'gt', 0.05, 30);
