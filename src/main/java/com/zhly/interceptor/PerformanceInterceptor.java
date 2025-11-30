package com.zhly.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 性能监控拦截器
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Component
public class PerformanceInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceInterceptor.class);
    private static final String START_TIME = "startTime";
    private static final long SLOW_REQUEST_THRESHOLD = 1000; // 1秒
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME, startTime);
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            String uri = request.getRequestURI();
            String method = request.getMethod();
            
            if (duration > SLOW_REQUEST_THRESHOLD) {
                logger.warn("慢请求检测: {} {} 耗时: {}ms", method, uri, duration);
            } else {
                logger.debug("请求完成: {} {} 耗时: {}ms", method, uri, duration);
            }
            
            // 记录性能指标
            recordPerformanceMetrics(uri, method, duration, response.getStatus());
        }
    }
    
    private void recordPerformanceMetrics(String uri, String method, long duration, int status) {
        // 这里可以集成监控系统，如Prometheus、Micrometer等
        // 记录请求次数、响应时间、错误率等指标
        logger.debug("性能指标: URI={}, Method={}, Duration={}ms, Status={}", 
                    uri, method, duration, status);
    }
}


















