package com.dashboard.gateway.controller;

import com.dashboard.common.constant.KafkaTopic;
import com.dashboard.common.dto.MetricData;
import com.dashboard.common.exception.BusinessException;
import com.dashboard.common.result.ErrorCode;
import com.dashboard.common.result.R;
import com.dashboard.gateway.aspect.CostTimeAspect.CostTime;
import com.dashboard.gateway.aspect.OperationLogAspect.OperationLog;
import com.dashboard.gateway.ratelimit.RateLimit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 指标数据上报接口
 *
 * 面试亮点：
 * 1. 统一响应封装 R<T>
 * 2. 异步发送 Kafka，不阻塞 HTTP 请求
 * 3. 注解式限流 @RateLimit + 耗时统计 @CostTime
 * 4. Swagger API 文档自动生成
 * 5. 参数校验 + 全局异常处理
 */
@Tag(name = "指标数据", description = "指标数据上报接口")
@RestController
@RequestMapping("/api/metric")
public class MetricController {

    private static final Logger log = LoggerFactory.getLogger(MetricController.class);

    @Autowired
    private KafkaTemplate<String, MetricData> kafkaTemplate;

    @Operation(summary = "单条上报", description = "上报单条指标数据到 Kafka")
    @PostMapping("/report")
    @RateLimit(qps = 50000)
    @CostTime(threshold = 200)
    public R<Map<String, Object>> report(@RequestBody MetricData data) {
        if (data.getMetricName() == null || data.getMetricName().isEmpty()) {
            throw new BusinessException(ErrorCode.METRIC_DATA_INVALID, "指标名称不能为空");
        }

        if (data.getTimestamp() == 0) {
            data.setTimestamp(System.currentTimeMillis());
        }

        // 异步发送到 Kafka
        CompletableFuture<SendResult<String, MetricData>> future =
                kafkaTemplate.send(KafkaTopic.METRIC_RAW, data.getMetricName(), data);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Kafka 发送失败: metric={}", data.getMetricName(), ex);
            } else {
                log.debug("Kafka 发送成功: topic={}, partition={}, offset={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });

        Map<String, Object> resp = new HashMap<>();
        resp.put("metric", data.getMetricName());
        resp.put("status", "accepted");
        return R.ok(resp);
    }

    @Operation(summary = "批量上报", description = "批量上报指标数据")
    @PostMapping("/batch")
    @RateLimit(qps = 20000)
    @CostTime(threshold = 500)
    public R<Map<String, Object>> batchReport(@RequestBody List<MetricData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "数据列表不能为空");
        }

        long now = System.currentTimeMillis();
        for (MetricData data : dataList) {
            if (data.getTimestamp() == 0) {
                data.setTimestamp(now);
            }
            kafkaTemplate.send(KafkaTopic.METRIC_RAW, data.getMetricName(), data);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("count", dataList.size());
        resp.put("status", "accepted");
        return R.ok(resp);
    }

    @Operation(summary = "查询指标", description = "根据指标名称查询最新聚合数据")
    @GetMapping("/query")
    public R<Map<String, Object>> query(
            @Parameter(description = "指标名称") @RequestParam String metricName) {
        // 此处简化，实际应从 Redis 或 DB 查询
        Map<String, Object> resp = new HashMap<>();
        resp.put("metricName", metricName);
        resp.put("source", "redis");
        return R.ok(resp);
    }

    @Operation(summary = "健康检查", description = "服务健康状态")
    @GetMapping("/health")
    public R<Map<String, Object>> health() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "UP");
        resp.put("service", "dashboard-gateway");
        resp.put("version", "1.0.0");
        return R.ok(resp);
    }
}
