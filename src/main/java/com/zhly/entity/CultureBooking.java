package com.zhly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 文旅预订实体
 */
@Data
@TableName("culture_booking")
public class CultureBooking {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String bookingNo;
    private Long userId;
    private Integer bookingType; // 1-文化体验 2-农家乐/民宿
    private Long itemId;
    private String itemName;
    private String itemImage;
    private Long packageId;
    private String packageName;
    private LocalDate bookingDate;
    private String bookingTime;
    private Integer peopleCount;
    private String contactName;
    private String contactPhone;
    private String contactIdCard;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private String specialRequirements;
    private Integer status; // 1-待确认 2-已确认 3-已完成 4-已取消 5-已退款
    private LocalDateTime confirmTime;
    private LocalDateTime completeTime;
    private LocalDateTime cancelTime;
    private String cancelReason;
    private LocalDateTime refundTime;
    private BigDecimal refundAmount;
    private Integer paymentStatus; // 1-未支付 2-已支付 3-已退款
    private LocalDateTime paymentTime;
    private String paymentNo;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}












































































