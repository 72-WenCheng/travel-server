package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券实体
 */
@Data
@TableName("coupon")
public class Coupon {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    private Integer type; // 1-满减券 2-折扣券 3-兑换券
    private Integer discountType; // 1-固定金额 2-折扣比例
    private BigDecimal discountValue;
    private BigDecimal minAmount;
    private BigDecimal maxDiscount;
    private Integer totalCount;
    private Integer remainingCount;
    private Integer perUserLimit;
    private Integer scope; // 1-全场通用 2-指定分类 3-指定商品
    private String scopeIds;
    private Integer validDays;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status; // 1-未开始 2-进行中 3-已结束 4-已下架
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}












































































