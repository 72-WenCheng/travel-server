package com.zhly.aspect;

import com.google.common.util.concurrent.RateLimiter;
import com.zhly.annotation.RateLimit;
import com.zhly.common.Result;
import com.zhly.config.RateLimitConfig;
import com.zhly.exception.BusinessException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流切面
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Aspect
@Component
public class RateLimitAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitAspect.class);
    
    @Autowired
    private RateLimitConfig rateLimitConfig;
    
    private final ConcurrentHashMap<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();
    
    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        try {
            // 获取限流器
            RateLimiter limiter = getRateLimiter(point, rateLimit);
            
            // 尝试获取许可
            if (limiter.tryAcquire()) {
                return point.proceed();
            } else {
                logger.warn("限流触发: {}", getRequestInfo());
                return Result.error("请求过于频繁，请稍后再试");
            }
        } catch (Exception e) {
            logger.error("限流处理异常: {}", e.getMessage(), e);
            return point.proceed();
        }
    }
    
    private RateLimiter getRateLimiter(ProceedingJoinPoint point, RateLimit rateLimit) {
        String key = getRateLimitKey(point, rateLimit);
        
        return rateLimiters.computeIfAbsent(key, k -> {
            double permitsPerSecond = rateLimit.permitsPerSecond();
            return RateLimiter.create(permitsPerSecond);
        });
    }
    
    private String getRateLimitKey(ProceedingJoinPoint point, RateLimit rateLimit) {
        StringBuilder key = new StringBuilder();
        
        // 获取方法信息
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();
        
        key.append(className).append(".").append(methodName);
        
        // 根据限流类型添加额外标识
        switch (rateLimit.limitType()) {
            case IP:
                key.append(":").append(getClientIp());
                break;
            case USER:
                key.append(":user:").append(getCurrentUserId());
                break;
            case CUSTOM:
                key.append(":custom:").append(getCustomKey(point));
                break;
            default:
                // GLOBAL类型不需要额外标识
                break;
        }
        
        return key.toString();
    }
    
    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
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
        } catch (Exception e) {
            logger.warn("获取客户端IP失败: {}", e.getMessage());
        }
        return "unknown";
    }
    
    private String getCurrentUserId() {
        // 这里可以从JWT token或session中获取用户ID
        // 暂时返回默认值
        return "anonymous";
    }
    
    private String getCustomKey(ProceedingJoinPoint point) {
        // 自定义限流键的生成逻辑
        // 可以根据方法参数、请求头等生成
        return "custom";
    }
    
    private String getRequestInfo() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return String.format("IP: %s, URI: %s", getClientIp(), request.getRequestURI());
            }
        } catch (Exception e) {
            logger.warn("获取请求信息失败: {}", e.getMessage());
        }
        return "unknown";
    }
}


















