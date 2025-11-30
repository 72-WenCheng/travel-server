package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * API调用日志实体
 * 
 * @author zhly
 * @since 2025-10-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("api_call_log")
public class ApiCallLog implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long apiConfigId;
    private String apiName;
    private String requestUrl;
    private String requestParams;
    private Integer responseCode;
    private String responseData;
    private Integer costTime;  // 耗时（毫秒）
    private Long userId;
    private String ipAddress;
    private Integer status;  // 0-失败，1-成功
    private String errorMsg;
    private LocalDateTime createTime;
}







