package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单信息实体
 * 
 * @author zhly
 * @since 2025-10-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("order_info")
public class OrderInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String orderNo;
    private Long userId;
    /**
     * 订单类型（1-住宿，2-餐饮，3-门票，4-商品，5-体验，6-演出）
     */
    private Integer orderType;
    
    private Long merchantId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal actualAmount;
    private Integer pointsEarned;
    private Integer pointsUsed;
    private String contactName;
    private String contactPhone;
    private LocalDate bookingDate;
    private String bookingTime;
    /**
     * 订单状态（0-待支付，1-已支付，2-已完成，3-已取消，4-已退款）
     */
    private Integer orderStatus;
    
    /**
     * 支付方式（1-微信，2-支付宝，3-银行卡）
     */
    private Integer paymentMethod;
    
    private LocalDateTime paymentTime;
    private String remarks;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}







