package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 文旅订单实体
 */
@Data
@TableName("culture_order")
public class CultureOrder {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String orderNo;
    private Long userId;
    private Integer productType; // 1-特色产品 2-文化体验 3-农家乐/民宿
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal productPrice;
    private Integer quantity;
    private BigDecimal totalAmount;
    private Long couponId;
    private BigDecimal couponDiscount;
    private BigDecimal finalAmount;
    private String contactName;
    private String contactPhone;
    private Long addressId;
    private String province;
    private String city;
    private String district;
    private String detailedAddress;
    private Integer invoiceType; // 1-不开发票 2-个人 3-企业
    private String invoiceTitle;
    private String invoiceTaxNo;
    private String invoiceEmail;
    private String buyerMessage;
    private Integer orderStatus; // 1-待支付 2-已支付 3-已发货 4-已完成 5-已取消 6-已退款
    private String paymentMethod;
    private LocalDateTime paymentTime;
    private String paymentNo;
    private LocalDateTime shipTime;
    private String shipNo;
    private String shipCompany;
    private LocalDateTime finishTime;
    private LocalDateTime cancelTime;
    private String cancelReason;
    private LocalDateTime refundTime;
    private BigDecimal refundAmount;
    private String refundReason;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}












































































