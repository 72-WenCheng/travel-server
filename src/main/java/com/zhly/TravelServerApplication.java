package com.zhly;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 旅游系统启动类
 * 
 * @author zhly
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@MapperScan({"com.zhly.mapper", "com.zhly.admin.mapper"})
public class TravelServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TravelServerApplication.class, args);
        System.out.println("=================================");
        System.out.println("旅游系统启动成功！");
        System.out.println("API文档地址: http://localhost:8070/api/doc.html");
        System.out.println("数据库监控: http://localhost:8070/api/druid");
        System.out.println("=================================");
    }
}
