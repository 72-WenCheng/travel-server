package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * AI生成记录实体
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("ai_generate_log")
public class AiGenerateLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private String requestContent;
    private String responseContent;
    private Integer generateType;
    private Integer costTokens;
    private Integer responseTime;  // 响应时间(毫秒)
    private Integer status;
    private LocalDateTime createTime;
}

