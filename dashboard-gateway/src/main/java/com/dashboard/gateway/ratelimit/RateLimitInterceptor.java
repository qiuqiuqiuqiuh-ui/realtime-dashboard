package com.dashboard.gateway.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流拦截器
 *
 * 面试亮点：
 * 1. 注解式限流，使用方便
 * 2. 每个接口独立限流实例，互不影响
 * 3. 返回 429 Too Many Requests
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    // 缓存每个接口的限流器实例
    private final ConcurrentHashMap<String, SlidingWindowRateLimiter> limiters = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
        if (rateLimit == null) {
            return true;
        }

        String key = handlerMethod.getBeanType().getSimpleName() + "." + handlerMethod.getMethod().getName();
        SlidingWindowRateLimiter limiter = limiters.computeIfAbsent(key,
                k -> new SlidingWindowRateLimiter(rateLimit.qps()));

        if (!limiter.tryAcquire()) {
            rejectRequest(response);
            return false;
        }

        return true;
    }

    private void rejectRequest(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\"}");
    }
}
