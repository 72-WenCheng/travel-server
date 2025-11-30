package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 积分明细实体
 * 
 * @author zhly
 * @since 2025-10-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("points_log")
public class PointsLog implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 积分变动（正数为增加，负数为减少）
     */
    private Integer points;
    
    /**
     * 行为类型
     * 1-登录，2-发布攻略，3-评论，4-点赞，5-收藏，
     * 6-分享，7-完善资料，8-邀请好友，9-消费，10-管理员调整
     */
    private Integer actionType;
    
    /**
     * 行为描述
     */
    private String actionDesc;
    
    /**
     * 关联类型（plan/attraction/comment等）
     */
    private String relatedType;
    
    /**
     * 关联ID
     */
    private Long relatedId;
    
    /**
     * 变动后余额
     */
    private Integer balanceAfter;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}







