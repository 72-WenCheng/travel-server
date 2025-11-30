package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论实体类
 */
@Data
@TableName("comment")
public class Comment implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 内容类型: PLAN-攻略, ATTRACTION-景点, CULTURE-文旅项目, MOMENT-动态
     */
    private String contentType;
    
    /**
     * 内容ID
     */
    private Long contentId;
    
    /**
     * 评论用户ID
     */
    private Long userId;
    
    /**
     * 父评论ID（顶级评论为null）
     */
    private Long parentId;
    
    /**
     * 回复给哪个用户（用于显示@xxx）
     */
    private Long replyToUserId;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 回复数
     */
    private Integer replyCount;
    
    /**
     * 状态: PUBLISHED-已发布, HIDDEN-已隐藏, DELETED-已删除
     */
    private String status;
    
    /**
     * 是否置顶
     */
    private Boolean isTop;
    
    /**
     * 是否热评
     */
    private Boolean isHot;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedTime;
    
    // ========== 关联查询字段（不存在于数据库） ==========
    
    /**
     * 评论用户信息
     */
    @TableField(exist = false)
    private String username;
    
    @TableField(exist = false)
    private String avatar;
    
    @TableField(exist = false)
    private Integer userPoints;            // 用户积分（用于前端根据升级指南计算等级）
    
    /**
     * 回复对象用户名
     */
    @TableField(exist = false)
    private String replyToUsername;
    
    /**
     * 内容标题（攻略标题、景点名称等）
     */
    @TableField(exist = false)
    private String contentTitle;
    
    /**
     * 当前用户是否已点赞
     */
    @TableField(exist = false)
    private Boolean hasLiked;
    
    /**
     * 是否为内容作者
     */
    @TableField(exist = false)
    private Boolean isAuthor;
    
    /**
     * 回复列表
     */
    @TableField(exist = false)
    private java.util.List<Comment> replies;
}
