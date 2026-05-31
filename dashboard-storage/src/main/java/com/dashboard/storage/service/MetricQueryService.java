package com.dashboard.storage.service;

import com.dashboard.common.result.PageResult;
import com.dashboard.storage.entity.MetricAggregationEntity;
import com.dashboard.storage.entity.MetricDataEntity;
import com.dashboard.storage.mapper.MetricAggregationMapper;
import com.dashboard.storage.mapper.MetricDataMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 指标数据查询服务
 */
@Service
public class MetricQueryService {

    @Autowired
    private MetricDataMapper metricDataMapper;

    @Autowired
    private MetricAggregationMapper aggregationMapper;

    /**
     * 分页查询指标数据
     */
    public PageResult<MetricDataEntity> queryMetrics(String metricName, int pageNum, int pageSize) {
        LambdaQueryWrapper<MetricDataEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetricDataEntity::getMetricName, metricName)
               .orderByDesc(MetricDataEntity::getCreatedAt);

        List<MetricDataEntity> records = metricDataMapper.selectList(
                wrapper.last("LIMIT " + pageSize + " OFFSET " + (pageNum - 1) * pageSize));
        long total = metricDataMapper.selectCount(wrapper);

        return PageResult.of(records, total, pageNum, pageSize);
    }

    /**
     * 查询最新聚合数据
     */
    public MetricAggregationEntity getLatestAggregation(String metricName) {
        return aggregationMapper.findLatest(metricName);
    }

    /**
     * 查询时间范围内的聚合数据
     */
    public List<MetricAggregationEntity> getAggregationHistory(String metricName,
                                                                 LocalDateTime start,
                                                                 LocalDateTime end) {
        return aggregationMapper.findByTimeRange(metricName, start, end);
    }

    /**
     * 统计各指标数据量
     */
    public List<Map<String, Object>> getMetricStats(LocalDateTime since) {
        return metricDataMapper.countByMetric(since);
    }
}
