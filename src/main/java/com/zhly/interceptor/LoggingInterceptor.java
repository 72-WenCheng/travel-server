package com.zhly.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Enumeration;

/**
 * 日志拦截器
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Component
public class LoggingInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 记录请求信息
        logger.info("请求开始: {} {} from {}", 
                   request.getMethod(), 
                   request.getRequestURI(), 
                   getClientIpAddress(request));
        
        // 记录请求头（敏感信息除外）
        logRequestHeaders(request);
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 记录响应信息
        logger.info("请求完成: {} {} 状态: {}", 
                   request.getMethod(), 
                   request.getRequestURI(), 
                   response.getStatus());
        
        if (ex != null) {
            logger.error("请求异常: {} {}", request.getRequestURI(), ex.getMessage(), ex);
        }
    }
    
    private void logRequestHeaders(HttpServletRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("请求头信息:");
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                // 过滤敏感信息
                if (!isSensitiveHeader(headerName)) {
                    logger.debug("  {}: {}", headerName, headerValue);
                }
            }
        }
    }
    
    private boolean isSensitiveHeader(String headerName) {
        String lowerName = headerName.toLowerCase();
        return lowerName.contains("authorization") || 
               lowerName.contains("cookie") || 
               lowerName.contains("password") ||
               lowerName.contains("token");
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}


















