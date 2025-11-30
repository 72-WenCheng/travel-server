package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 文化体验实体
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("culture_experience")
public class CultureExperience {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 体验名称
     */
    private String name;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 位置
     */
    private String location;
    
    /**
     * 价格
     */
    private BigDecimal price;
    
    /**
     * 时长
     */
    private String duration;
    
    /**
     * 评分
     */
    private BigDecimal rating;
    
    /**
     * 状态：active-上架, inactive-下架
     */
    private String status;
    
    /**
     * 图片列表（JSON数组）
     */
    private String images;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 流程（JSON数组）
     */
    private String flow;
    
    /**
     * 注意事项（JSON数组）
     */
    private String notes;
    
    /**
     * 评论（JSON数组）
     */
    private String reviews;
    
    /**
     * 浏览量
     */
    private Integer viewCount;
    
    /**
     * 预订量
     */
    private Integer orderCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}











































































