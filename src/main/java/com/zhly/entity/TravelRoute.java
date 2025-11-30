package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 旅游路线实体
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("travel_route")
public class TravelRoute {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long planId;
    private Integer dayNumber;
    private Integer orderNumber;
    private Long attractionId;
    private String attractionName;
    private String activityDescription;
    private LocalTime startTime;
    private LocalTime endTime;
    private String transportation;
    private String notes;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}


















