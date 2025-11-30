package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户操作日志实体类
 */
@Data
@TableName("user_operation_log")
public class UserOperationLog {
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 操作类型：VIEW-查看，EDIT-编辑，DELETE-删除，CREATE-创建等
     */
    private String operationType;

    /**
     * 操作模块：USER-用户，PLAN-攻略，ATTRACTION-景点等
     */
    private String operationModule;

    /**
     * 操作内容
     */
    private String operationContent;

    /**
     * 操作IP
     */
    private String operationIp;

    /**
     * 操作时间
     */
    private LocalDateTime operationTime;

    /**
     * 状态：1-成功，0-失败
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}












































































