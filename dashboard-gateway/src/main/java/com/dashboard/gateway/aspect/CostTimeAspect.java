package com.dashboard.gateway.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口耗时统计切面
 *
 * 面试亮点：
 * 1. AOP 统计接口耗时，无侵入
 * 2. 超过阈值自动 WARN 告警
 * 3. 自定义注解 @CostTime，使用简单
 */
@Aspect
@Component
public class CostTimeAspect {

    private static final Logger log = LoggerFactory.getLogger(CostTimeAspect.class);

    @Pointcut("@annotation(CostTimeAspect.CostTime)")
    public void costTimePointcut() {}

    @Around("costTimePointcut()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        CostTime costTime = signature.getMethod().getAnnotation(CostTime.class);
        int threshold = costTime != null ? costTime.threshold() : 1000;

        long start = System.currentTimeMillis();
        try {
            return pjp.proceed();
        } finally {
            long cost = System.currentTimeMillis() - start;
            String methodName = signature.getDeclaringType().getSimpleName() + "." + signature.getName();

            if (cost > threshold) {
                log.warn("[慢接口] {} 耗时 {}ms (阈值 {}ms)", methodName, cost, threshold);
            } else {
                log.debug("[接口耗时] {} {}ms", methodName, cost);
            }
        }
    }

    /**
     * 耗时统计注解
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CostTime {
        /** 慢接口阈值 (毫秒)，超过此值打印 WARN 日志 */
        int threshold() default 500;
    }
}
