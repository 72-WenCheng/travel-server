package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 景点实体
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("attraction")
public class Attraction {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    private String description;
    private Integer type;
    private String city;
    private String province;
    private String address;
    private String phone;
    private String website;
    private java.math.BigDecimal longitude;
    private java.math.BigDecimal latitude;
    private java.math.BigDecimal ticketPrice;
    private String openTime;
    private Integer suggestedDuration;
    private Integer rating;
    private java.math.BigDecimal score;
    private String coverImage;
    private String images;
    private String tags;
    private String features;
    private String transportation;
    private String nearbyFood;
    private String nearbyHotel;
    private String bestSeason;
    private String notes;
    private Integer viewCount;
    private Integer collectCount;
    private Integer commentCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}