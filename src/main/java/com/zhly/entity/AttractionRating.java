package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 景点评分记录
 */
@Data
@TableName("attraction_rating")
public class AttractionRating {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 景点ID
     */
    private Long attractionId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 评分（1-5）
     */
    private Integer score;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最近更新时间
     */
    private LocalDateTime updateTime;
}

