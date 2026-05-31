package com.dashboard.storage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dashboard.storage.entity.MetricAggregationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聚合数据 Mapper
 */
@Mapper
public interface MetricAggregationMapper extends BaseMapper<MetricAggregationEntity> {

    /**
     * 查询最新的聚合数据
     */
    @Select("SELECT * FROM metric_aggregation WHERE metric_name = #{metricName} " +
            "ORDER BY window_end DESC LIMIT 1")
    MetricAggregationEntity findLatest(@Param("metricName") String metricName);

    /**
     * 查询时间范围内的聚合数据
     */
    @Select("SELECT * FROM metric_aggregation WHERE metric_name = #{metricName} " +
            "AND window_start BETWEEN #{start} AND #{end} ORDER BY window_start ASC")
    List<MetricAggregationEntity> findByTimeRange(
            @Param("metricName") String metricName,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
