package com.zhly.config;

import com.zhly.security.ApiRateLimitInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC配置类
 * 用于配置静态资源访问
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${travel.upload.path:./upload/}")
    private String uploadPath;

    private final ApiRateLimitInterceptor apiRateLimitInterceptor;

    public WebMvcConfig(ApiRateLimitInterceptor apiRateLimitInterceptor) {
        this.apiRateLimitInterceptor = apiRateLimitInterceptor;
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 确保上传目录存在
        File uploadDir = new File(uploadPath);
        
        // 如果是相对路径，转换为项目根目录下的绝对路径
        if (!uploadDir.isAbsolute()) {
            String projectRoot = System.getProperty("user.dir");
            uploadDir = new File(projectRoot, uploadPath);
        }
        
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        // 配置上传文件的访问路径
        String absolutePath = uploadDir.getAbsolutePath();
        if (!absolutePath.endsWith(File.separator)) {
            absolutePath += File.separator;
        }
        
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + absolutePath);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiRateLimitInterceptor)
                .addPathPatterns("/api/**");
    }
}

