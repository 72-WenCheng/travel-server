package com.zhly.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.zhly.interceptor.PerformanceInterceptor;
import com.zhly.interceptor.LoggingInterceptor;

/**
 * 监控配置类
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Configuration
public class MonitoringConfig implements WebMvcConfigurer {
    
    @Bean
    public PerformanceInterceptor performanceInterceptor() {
        return new PerformanceInterceptor();
    }
    
    @Bean
    public LoggingInterceptor loggingInterceptor() {
        return new LoggingInterceptor();
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 性能监控拦截器
        registry.addInterceptor(performanceInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/health/**", "/api/info/**");
        
        // 日志拦截器
        registry.addInterceptor(loggingInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/health/**", "/api/info/**");
    }
}


















