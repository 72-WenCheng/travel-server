package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户注销申请记录
 */
@Data
@TableName("user_deactivate_request")
public class UserDeactivateRequest {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("reason")
    private String reason;

    /**
     * 0-待生效，1-用户取消，2-已完成
     */
    @TableField("status")
    private Integer status;

    @TableField("request_time")
    private LocalDateTime requestTime;

    @TableField("deadline_time")
    private LocalDateTime deadlineTime;

    @TableField("cancel_time")
    private LocalDateTime cancelTime;

    @TableField("complete_time")
    private LocalDateTime completeTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}














