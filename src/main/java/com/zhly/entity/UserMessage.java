package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户私信消息实体
 *
 * @author zhly
 * @since 2025-11-19
 */
@Data
@TableName("user_message")
public class UserMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话标识（由双方用户ID组成，方便快速查询）
     */
    private String conversationId;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息状态：0-未读，1-已读
     */
    private Integer status;

    /**
     * 已读时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime readTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    // ======== 以下字段为关联展示使用，不在数据库中 ========

    @TableField(exist = false)
    private String senderNickname;

    @TableField(exist = false)
    private String senderAvatar;

    @TableField(exist = false)
    private String receiverNickname;

    @TableField(exist = false)
    private String receiverAvatar;

    /**
     * 当前消息是否为我发送，用于前端快速判断展示方向
     */
    @TableField(exist = false)
    private Boolean isOutgoing;
}

