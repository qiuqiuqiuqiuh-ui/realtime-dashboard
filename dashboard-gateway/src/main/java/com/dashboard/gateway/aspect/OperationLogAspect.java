package com.dashboard.gateway.aspect;

import com.alibaba.fastjson2.JSON;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志切面
 *
 * 面试亮点：
 * 1. AOP 实现操作审计日志，零侵入
 * 2. 记录请求参数、响应结果、耗时、IP
 * 3. 生产环境可扩展写入数据库
 */
@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    @Pointcut("@annotation(OperationLogAspect.OperationLog)")
    public void operationLogPointcut() {}

    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String ip = "unknown";
        String uri = "unknown";
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            ip = getClientIp(request);
            uri = request.getRequestURI();
        }

        String methodName = pjp.getSignature().toShortString();
        Object[] args = pjp.getArgs();

        long start = System.currentTimeMillis();
        Object result = null;
        boolean success = true;
        try {
            result = pjp.proceed();
            return result;
        } catch (Throwable e) {
            success = false;
            throw e;
        } finally {
            long cost = System.currentTimeMillis() - start;
            log.info("[操作日志] method={}, ip={}, uri={}, args={}, result={}, success={}, cost={}ms",
                    methodName, ip, uri,
                    JSON.toJSONString(args),
                    success ? JSON.toJSONString(result) : "EXCEPTION",
                    success, cost);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 操作日志注解
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface OperationLog {
        /** 操作描述 */
        String value() default "";
    }
}
