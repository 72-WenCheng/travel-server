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
 * 推荐记录实体
 * 
 * @author zhly
 * @since 2025-10-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("recommendation_log")
public class RecommendationLog implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    /**
     * 推荐类型（1-景点，2-攻略，3-路线，4-酒店，5-商品）
     */
    private Integer recommendType;
    
    private Long recommendId;
    private String recommendReason;
    private String algorithm;
    private BigDecimal score;
    private Integer isClicked;  // 0-否，1-是
    private LocalDateTime clickedTime;
    private Integer isLiked;    // 0-否，1-是
    private LocalDateTime createTime;
}







