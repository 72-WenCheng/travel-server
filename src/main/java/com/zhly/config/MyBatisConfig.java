package com.zhly.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis配置类 - 使用MyBatis Plus自动配置
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Configuration
@MapperScan("com.zhly.mapper")
public class MyBatisConfig {
    // 使用MyBatis Plus的自动配置
    // 不需要手动配置SqlSessionFactory，MyBatis Plus会自动处理
}
