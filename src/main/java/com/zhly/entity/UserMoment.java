package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户动态实体
 * 
 * @author zhly
 * @since 2025-10-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_moment")
public class UserMoment implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 动态类型（1-发布攻略，2-收藏景点，3-打卡，4-评论，5-点赞）
     */
    private Integer momentType;
    
    /**
     * 动态内容
     */
    private String content;
    
    /**
     * 图片列表（JSON）
     */
    private String images;
    
    /**
     * 位置
     */
    private String location;
    
    /**
     * 关联类型
     */
    private String relatedType;
    
    /**
     * 关联ID
     */
    private Long relatedId;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 评论数
     */
    private Integer commentCount;
    
    /**
     * 浏览量
     */
    private Integer viewCount;
    
    /**
     * 是否公开（0-私密，1-公开）
     */
    private Integer isPublic;
    
    /**
     * 状态（0-已删除，1-正常，2-隐藏）
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}







