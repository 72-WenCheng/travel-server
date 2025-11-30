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
 * 酒店信息实体
 * 
 * @author zhly
 * @since 2025-10-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hotel")
public class Hotel implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    private Integer starRating;
    private String province;
    private String city;
    private String district;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String phone;
    private String priceRange;
    private BigDecimal minPrice;
    private String images;
    private String coverImage;
    private String facilities;
    private String description;
    private BigDecimal score;
    private Integer reviewCount;
    private Integer bookingCount;
    private String apiSource;
    private String apiHotelId;
    private String apiLink;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}







