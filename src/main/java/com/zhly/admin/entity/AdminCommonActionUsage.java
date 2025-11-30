package com.zhly.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员常用操作统计实体
 */
@Data
@TableName("admin_common_action_usage")
public class AdminCommonActionUsage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 管理员ID
     */
    private Long adminId;

    /**
     * 操作编码
     */
    private String actionCode;

    /**
     * 操作显示名称
     */
    private String actionName;

    /**
     * 对应的前端路由
     */
    private String actionPath;

    /**
     * 图标标识（对应前端Icon）
     */
    private String actionIcon;

    /**
     * 操作描述
     */
    private String actionDesc;

    /**
     * 点击次数
     */
    private Long clickCount;

    /**
     * 最后点击时间
     */
    private LocalDateTime lastClickTime;

    /**
     * 记录创建时间
     */
    private LocalDateTime createTime;
}

