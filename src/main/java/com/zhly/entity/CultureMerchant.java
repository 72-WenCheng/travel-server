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
 * 文旅商家实体
 * 
 * @author zhly
 * @since 2025-10-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("culture_merchant")
public class CultureMerchant implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String merchantName;
    /**
     * 商家类型（1-民宿，2-特产店，3-手工艺品，4-文化体验馆，5-餐饮，6-演出场所）
     */
    private Integer merchantType;
    
    private String province;
    private String city;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String contactPerson;
    private String contactPhone;
    private String businessHours;
    private String images;
    private String coverImage;
    private String description;
    private String features;
    private String priceRange;
    private BigDecimal rating;
    private Integer reviewCount;
    private Integer orderCount;
    private String certification;
    private Integer status;  // 0-停业，1-营业，2-审核中
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}







