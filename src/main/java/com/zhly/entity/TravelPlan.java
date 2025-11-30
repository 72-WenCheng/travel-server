package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * 旅游攻略实体
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("travel_plan")
public class TravelPlan {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 基本信息
    private String title;                    // 攻略标题
    private String destination;              // 目的地
    private Integer days;                    // 天数
    private Integer personCount;             // 出行人数
    private BigDecimal budget;               // 预算
    private String description;              // 攻略描述
    private String content;                  // 攻略详细内容
    private String itinerary;                // 行程安排(JSON)
    private String costDetail;               // 费用明细(JSON)
    private String notice;                   // 注意事项
    private String images;                   // 图片列表(逗号分隔)
    private String tags;                     // 标签
    private String bestSeason;               // 最佳季节
    private String travelType;               // 旅行方式
    private String suitableFor;              // 适合人群
    private Integer difficultyLevel;         // 难度等级：1-5
    
    // 状态信息
    private Integer status;                  // 发布状态：0-草稿，1-已发布，2-已下架
    
    @TableField(insertStrategy = FieldStrategy.ALWAYS, updateStrategy = FieldStrategy.IGNORED)
    private Integer auditStatus;             // 审核状态：NULL-草稿，0-待审核，1-通过，2-拒绝
    
    private String rejectReason;             // 拒绝原因
    
    // 作者信息
    private Long authorId;                   // 作者ID
    
    // 统计信息
    private Integer viewCount;               // 浏览量
    private Integer likeCount;               // 点赞/收藏数
    private Integer commentCount;            // 评论数
    
    // 时间信息
    private LocalDateTime createTime;        // 创建时间
    private LocalDateTime updateTime;        // 更新时间
    private LocalDateTime publishTime;       // 发布时间
    
    // 非数据库字段，用于关联查询
    @TableField(exist = false)
    private String author;                   // 作者昵称
    
    @TableField(exist = false)
    private String authorAvatar;             // 作者头像
    
    @TableField(exist = false)
    private String username;                 // 作者用户名
    
    @TableField(exist = false)
    private Integer authorPoints;            // 作者积分（用于前端根据升级指南计算等级）
    
    @TableField(exist = false)
    private String coverImage;               // 封面图片（从images字段提取，非数据库字段）
}