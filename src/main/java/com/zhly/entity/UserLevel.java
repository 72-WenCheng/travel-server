package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户等级实体
 * 
 * @author zhly
 * @since 2025-10-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_level")
public class UserLevel implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 等级名称（青铜、白银、黄金、铂金、钻石、王者）
     */
    @TableField("level_name")
    private String levelName;
    
    /**
     * 等级代码（1-6）
     */
    @TableField("level_code")
    private Integer levelCode;
    
    /**
     * 所需积分
     */
    @TableField("required_points")
    private Integer requiredPoints;
    
    /**
     * 等级图标
     */
    @TableField("level_icon")
    private String levelIcon;
    
    /**
     * 等级颜色
     */
    @TableField("level_color")
    private String levelColor;
    
    /**
     * 等级权益（JSON格式）
     */
    @TableField("benefits")
    private String benefits;
    
    /**
     * 是否可发布攻略（0-否，1-是）
     */
    @TableField("can_post_plan")
    private Integer canPostPlan;
    
    /**
     * 是否可评论（0-否，1-是）
     */
    @TableField("can_comment")
    private Integer canComment;
    
    /**
     * 是否可查看精品内容（0-否，1-是）
     */
    @TableField("can_view_premium")
    private Integer canViewPremium;
    
    /**
     * 每日发布限制
     */
    @TableField("daily_post_limit")
    private Integer dailyPostLimit;
    
    /**
     * 每日评论限制
     */
    @TableField("daily_comment_limit")
    private Integer dailyCommentLimit;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}







