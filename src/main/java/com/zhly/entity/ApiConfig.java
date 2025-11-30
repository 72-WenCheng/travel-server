package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * API配置实体
 * 
 * @author zhly
 * @since 2025-10-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("api_config")
public class ApiConfig implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String apiName;
    /**
     * API类型（map/hotel/transportation/weather/payment）
     */
    private String apiType;
    
    /**
     * 服务商（amap/baidu/ctrip/12306等）
     */
    private String provider;
    
    private String apiKey;
    private String apiSecret;
    private String endpoint;
    private String configJson;
    private Integer rateLimit;  // 调用限制（次/天）
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}







