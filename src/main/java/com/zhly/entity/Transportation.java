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
 * 交通方式实体
 * 
 * @author zhly
 * @since 2025-10-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("transportation")
public class Transportation implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 交通类型（1-飞机，2-火车，3-汽车，4-地铁，5-公交，6-出租车，7-自驾）
     */
    private Integer transType;
    
    private String fromCity;
    private String toCity;
    private String transNumber;
    private String departureStation;
    private String arrivalStation;
    private String departureTime;
    private String arrivalTime;
    private String duration;
    private String priceRange;
    private BigDecimal minPrice;
    private String apiSource;
    private String apiLink;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}







