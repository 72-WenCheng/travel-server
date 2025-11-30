package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 智能路线实体
 * 
 * @author zhly
 * @since 2025-10-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("smart_route")
public class SmartRoute implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String routeName;
    private String startCity;
    private String destination;
    private Integer days;
    private Integer routeType;  // 1-经典路线，2-深度游，3-亲子游，4-情侣游，5-美食游
    private Integer difficulty;  // 1-轻松，2-适中，3-挑战
    private Integer budgetLevel; // 1-经济，2-舒适，3-豪华
    private BigDecimal estimatedCost;
    private String routePoints;  // JSON：包含景点、酒店、交通
    private String daySchedule;  // JSON：每日行程
    private String coverImage;
    private String images;
    private String description;
    private String highlights;
    private String tips;
    private Long creatorId;
    private Integer isOfficial;  // 0-用户创建，1-官方路线
    private Integer viewCount;
    private Integer collectCount;
    private Integer useCount;
    private BigDecimal rating;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}







