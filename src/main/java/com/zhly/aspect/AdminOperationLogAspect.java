package com.zhly.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhly.annotation.AdminOperationLog;
import com.zhly.service.UserOperationLogService;
import com.zhly.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 管理端操作日志切面
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminOperationLogAspect {

    private final UserOperationLogService userOperationLogService;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Around("@annotation(adminOperationLog)")
    public Object recordAdminOperation(ProceedingJoinPoint joinPoint, AdminOperationLog adminOperationLog) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        Long userId = getUserId(request);
        String operationIp = request != null ? getClientIpAddress(request) : "unknown";
        String content = buildOperationContent(adminOperationLog, joinPoint, request);

        boolean success = true;
        Throwable throwable = null;
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            success = false;
            throwable = ex;
            throw ex;
        } finally {
            try {
                userOperationLogService.recordOperationLog(
                        userId,
                        adminOperationLog.type(),
                        adminOperationLog.module(),
                        content,
                        operationIp,
                        success,
                        success ? null : (throwable != null ? throwable.getMessage() : "操作失败")
                );
            } catch (Exception logEx) {
                log.warn("记录操作日志失败: {}", logEx.getMessage());
            }
        }
        return result;
    }

    private Long getUserId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object attr = request.getAttribute("userId");
        if (attr instanceof Long) {
            return (Long) attr;
        }
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                if (jwtUtil.validateToken(token)) {
                    return jwtUtil.getUserIdFromToken(token);
                }
            } catch (Exception e) {
                log.debug("解析token获取用户ID失败: {}", e.getMessage());
            }
        }
        return null;
    }

    private String buildOperationContent(AdminOperationLog annotation, ProceedingJoinPoint joinPoint, HttpServletRequest request) {
        if (annotation.description() != null && !annotation.description().isEmpty()) {
            return annotation.description();
        }
        StringBuilder builder = new StringBuilder();
        builder.append("调用接口: ");
        if (request != null) {
            builder.append(request.getMethod()).append(" ").append(request.getRequestURI());
        } else {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            builder.append(signature.getDeclaringTypeName()).append(".").append(signature.getName());
        }
        try {
            String argsJson = objectMapper.writeValueAsString(joinPoint.getArgs());
            builder.append(", 参数: ").append(argsJson);
        } catch (Exception ignored) {
        }
        return builder.toString();
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"};
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}

































