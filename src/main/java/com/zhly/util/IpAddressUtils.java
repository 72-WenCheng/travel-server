package com.zhly.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * IP地址工具类
 * 用于从HttpServletRequest中解析客户端真实IP地址
 * 
 * @author zhly
 */
public class IpAddressUtils {
    
    /**
     * 解析客户端真实IP地址
     * 支持多种代理服务器场景（Nginx、Apache、负载均衡器等）
     * 
     * @param request HttpServletRequest对象
     * @return 客户端IP地址，如果无法获取则返回"unknown"
     */
    public static String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        
        // 优先级顺序：X-Forwarded-For > X-Real-IP > Proxy-Client-IP > WL-Proxy-Client-IP > RemoteAddr
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 如果是多IP（通过代理链），取第一个IP（客户端真实IP）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        // 处理IPv6本地地址
        if (ip != null && ip.equals("0:0:0:0:0:0:0:1")) {
            ip = "127.0.0.1";
        }
        
        return ip != null ? ip : "unknown";
    }
    
    /**
     * 判断是否是内网IP地址
     * 
     * @param ip IP地址
     * @return 如果是内网IP返回true，否则返回false
     */
    public static boolean isPrivateIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return true;
        }
        
        // 检查IPv6本地地址
        if (ip.contains(":")) {
            return ip.equals("::1") || ip.startsWith("fe80:") || ip.startsWith("fc00:");
        }
        
        // 检查IPv4内网地址
        return ip.equals("127.0.0.1") || ip.equals("localhost") ||
               ip.startsWith("192.168.") || ip.startsWith("10.") ||
               ip.startsWith("172.16.") || ip.startsWith("172.17.") ||
               ip.startsWith("172.18.") || ip.startsWith("172.19.") ||
               ip.startsWith("172.20.") || ip.startsWith("172.21.") ||
               ip.startsWith("172.22.") || ip.startsWith("172.23.") ||
               ip.startsWith("172.24.") || ip.startsWith("172.25.") ||
               ip.startsWith("172.26.") || ip.startsWith("172.27.") ||
               ip.startsWith("172.28.") || ip.startsWith("172.29.") ||
               ip.startsWith("172.30.") || ip.startsWith("172.31.");
    }
}


















