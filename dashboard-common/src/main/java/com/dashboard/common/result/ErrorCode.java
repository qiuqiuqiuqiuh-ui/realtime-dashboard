package com.dashboard.common.result;

/**
 * 错误码枚举
 *
 * 设计规范：
 * - 2xx: 成功
 * - 4xx: 客户端错误 (参数、认证、限流)
 * - 5xx: 服务端错误 (系统异常、依赖失败)
 * - 1xxx: 业务错误 (消息队列、指标相关)
 * - 2xxx: 数据错误
 */
public enum ErrorCode {

    // 成功
    SUCCESS(200, "成功"),

    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    RATE_LIMITED(429, "请求过于频繁，请稍后再试"),

    // 服务端错误 5xx
    INTERNAL_ERROR(500, "系统内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),

    // 消息队列错误 1xxx
    KAFKA_SEND_FAIL(1001, "消息发送失败"),
    KAFKA_CONSUME_FAIL(1002, "消息消费失败"),

    // 指标数据错误 2xxx
    METRIC_NOT_FOUND(2001, "指标不存在"),
    METRIC_DATA_INVALID(2002, "指标数据格式错误"),
    AGGREGATION_FAIL(2003, "聚合计算失败"),

    // 缓存错误 3xxx
    REDIS_ERROR(3001, "Redis 操作失败"),

    // WebSocket 错误 4xxx
    WS_CONNECT_FAIL(4001, "WebSocket 连接失败"),
    WS_SEND_FAIL(4002, "WebSocket 消息发送失败");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
}
