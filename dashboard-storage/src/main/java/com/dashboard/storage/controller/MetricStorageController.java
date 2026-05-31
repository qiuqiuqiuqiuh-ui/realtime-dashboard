package com.dashboard.storage.controller;

import com.dashboard.common.result.R;
import com.dashboard.storage.entity.MetricAggregationEntity;
import com.dashboard.storage.entity.MetricDataEntity;
import com.dashboard.storage.service.MetricQueryService;
import com.dashboard.common.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 数据存储查询接口
 */
@Tag(name = "数据查询", description = "指标数据持久化查询")
@RestController
@RequestMapping("/api/storage")
public class MetricStorageController {

    @Autowired
    private MetricQueryService queryService;

    @Operation(summary = "分页查询指标数据")
    @GetMapping("/metrics")
    public R<PageResult<MetricDataEntity>> queryMetrics(
            @Parameter(description = "指标名称") @RequestParam String metricName,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(queryService.queryMetrics(metricName, pageNum, pageSize));
    }

    @Operation(summary = "查询最新聚合数据")
    @GetMapping("/aggregation/latest")
    public R<MetricAggregationEntity> getLatest(
            @Parameter(description = "指标名称") @RequestParam String metricName) {
        return R.ok(queryService.getLatestAggregation(metricName));
    }

    @Operation(summary = "查询聚合历史")
    @GetMapping("/aggregation/history")
    public R<List<MetricAggregationEntity>> getHistory(
            @Parameter(description = "指标名称") @RequestParam String metricName,
            @Parameter(description = "开始时间") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @Parameter(description = "结束时间") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return R.ok(queryService.getAggregationHistory(metricName, start, end));
    }

    @Operation(summary = "指标统计概览")
    @GetMapping("/stats")
    public R<List<Map<String, Object>>> getStats(
            @Parameter(description = "统计起始时间") @RequestParam(defaultValue = "24") int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return R.ok(queryService.getMetricStats(since));
    }

    @GetMapping("/health")
    public R<String> health() {
        return R.ok("dashboard-storage is UP");
    }
}
