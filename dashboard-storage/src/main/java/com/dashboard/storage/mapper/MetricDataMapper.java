package com.dashboard.storage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dashboard.storage.entity.MetricDataEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 指标数据 Mapper
 */
@Mapper
public interface MetricDataMapper extends BaseMapper<MetricDataEntity> {

    /**
     * 按指标名称和时间范围查询
     */
    @Select("SELECT * FROM metric_data WHERE metric_name = #{metricName} " +
            "AND created_at BETWEEN #{start} AND #{end} ORDER BY created_at DESC LIMIT #{limit}")
    List<MetricDataEntity> findByTimeRange(
            @Param("metricName") String metricName,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("limit") int limit);

    /**
     * 统计各指标的数据量
     */
    @Select("SELECT metric_name, COUNT(*) as count FROM metric_data " +
            "WHERE created_at >= #{since} GROUP BY metric_name")
    List<Map<String, Object>> countByMetric(@Param("since") LocalDateTime since);
}
