package com.dashboard.gateway.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流注解 - 标注在 Controller 方法上
 *
 * 使用方式: @RateLimit(qps = 1000)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    /** 每秒最大请求数 */
    int qps() default 1000;
}
