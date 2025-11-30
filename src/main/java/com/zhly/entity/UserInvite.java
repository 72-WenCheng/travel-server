package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户邀请实体
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Data
@TableName("user_invite")
public class UserInvite implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 邀请人ID
     */
    private Long inviterId;
    
    /**
     * 被邀请人ID
     */
    private Long inviteeId;
    
    /**
     * 邀请码
     */
    private String inviteCode;
    
    /**
     * 是否已奖励积分（0-未奖励，1-已奖励）
     */
    private Integer rewarded;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 奖励时间
     */
    private LocalDateTime rewardTime;
}







































































