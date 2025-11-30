package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动实体类
 */
@Data
@TableName("activity")
public class Activity {
    /**
     * 活动ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 活动标题
     */
    private String title;

    /**
     * 活动副标题
     */
    private String subtitle;

    /**
     * 活动描述
     */
    private String description;

    /**
     * 封面图片
     */
    private String coverImage;

    /**
     * 活动图片（JSON数组）
     */
    private String images;

    /**
     * 活动类型：1-限时优惠，2-新品推荐，3-节日活动，4-特色路线，5-打卡挑战
     */
    private Integer activityType;

    /**
     * 活动开始时间
     */
    private LocalDateTime startTime;

    /**
     * 活动结束时间
     */
    private LocalDateTime endTime;

    /**
     * 活动地点
     */
    private String location;

    /**
     * 关联类型：1-景点，2-攻略，3-文旅项目
     */
    private Integer relatedType;

    /**
     * 关联ID列表（JSON数组）
     */
    private String relatedIds;

    /**
     * 优惠信息
     */
    private String discountInfo;

    /**
     * 活动规则
     */
    private String rules;

    /**
     * 状态：0-草稿，1-进行中，2-已结束，3-已下架
     */
    private Integer status;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 参与人数
     */
    private Integer participantCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}







