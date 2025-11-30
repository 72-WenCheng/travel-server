package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 通知实体
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("notification")
public class Notification {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 接收通知的用户ID
     */
    private Long userId;
    
    /**
     * 通知类型（1=评论回复, 2=评论点赞, 3=系统通知, 等）
     */
    private Integer type;
    
    /**
     * 通知标题
     */
    private String title;
    
    /**
     * 通知内容
     */
    private String content;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 关联类型（COMMENT, PLAN, 等）
     */
    private String relatedType;
    
    /**
     * 关联ID
     */
    private Long relatedId;
    
    /**
     * 链接地址
     */
    private String linkUrl;
    
    /**
     * 是否已读（0=未读, 1=已读）
     */
    private Integer isRead;
    
    /**
     * 已读时间
     */
    private LocalDateTime readTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}












