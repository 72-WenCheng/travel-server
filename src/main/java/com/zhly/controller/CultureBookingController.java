package com.zhly.controller;

import com.zhly.common.Result;
import com.zhly.entity.CultureBooking;
import com.zhly.entity.CultureExperience;
import com.zhly.mapper.CultureBookingMapper;
import com.zhly.service.CultureExperienceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 用户端-文旅预订控制器
 */
@Tag(name = "用户端-文旅预订")
@RestController
@RequestMapping("/api/culture/booking")
@RequiredArgsConstructor
public class CultureBookingController {
    
    private final CultureBookingMapper cultureBookingMapper;
    private final CultureExperienceService cultureExperienceService;
    
    @Operation(summary = "创建预订")
    @PostMapping
    public Result<String> createBooking(@RequestBody Map<String, Object> bookingData) {
        try {
            // 获取体验信息
            Long experienceId = Long.valueOf(bookingData.get("experienceId").toString());
            CultureExperience experience = cultureExperienceService.getExperienceById(experienceId);
            if (experience == null) {
                return Result.error("体验不存在");
            }
            if (!"active".equals(experience.getStatus())) {
                return Result.error("体验已下架，无法预订");
            }
            
            // 创建预订记录
            CultureBooking booking = new CultureBooking();
            booking.setBookingNo(generateBookingNo());
            booking.setUserId(getCurrentUserId()); // 需要从token中获取
            booking.setBookingType(1); // 1-文化体验
            booking.setItemId(experienceId);
            booking.setItemName(experience.getName());
            booking.setItemImage(experience.getImages());
            booking.setBookingDate(LocalDate.parse(bookingData.get("date").toString()));
            booking.setBookingTime(bookingData.get("timeSlot").toString());
            booking.setPeopleCount(Integer.valueOf(bookingData.get("participants").toString()));
            booking.setContactName(bookingData.get("contactName").toString());
            booking.setContactPhone(bookingData.get("contactPhone").toString());
            booking.setSpecialRequirements(bookingData.get("notes") != null ? bookingData.get("notes").toString() : null);
            booking.setPrice(experience.getPrice());
            booking.setTotalAmount(new BigDecimal(bookingData.get("totalAmount").toString()));
            booking.setStatus(1); // 1-待确认
            booking.setPaymentStatus(1); // 1-未支付
            booking.setCreateTime(LocalDateTime.now());
            booking.setUpdateTime(LocalDateTime.now());
            
            cultureBookingMapper.insert(booking);
            
            // 更新体验的预订量
            cultureExperienceService.updateOrderCount(experienceId);
            
            return Result.success("预订成功");
        } catch (Exception e) {
            return Result.error("预订失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "获取我的预订列表")
    @GetMapping("/my")
    public Result<List<CultureBooking>> getMyBookings(
            @RequestParam(required = false) Integer status) {
        try {
            Long userId = getCurrentUserId(); // 需要从token中获取
            List<CultureBooking> bookings = cultureBookingMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CultureBooking>()
                    .eq("user_id", userId)
                    .eq(status != null, "status", status)
                    .orderByDesc("create_time")
            );
            return Result.success(bookings);
        } catch (Exception e) {
            return Result.error("获取预订列表失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "获取预订详情")
    @GetMapping("/{id}")
    public Result<CultureBooking> getBooking(@PathVariable Long id) {
        try {
            CultureBooking booking = cultureBookingMapper.selectById(id);
            if (booking == null) {
                return Result.error("预订不存在");
            }
            // 验证是否是当前用户的预订
            Long userId = getCurrentUserId();
            if (!userId.equals(booking.getUserId())) {
                return Result.error("无权访问");
            }
            return Result.success(booking);
        } catch (Exception e) {
            return Result.error("获取预订详情失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "取消预订")
    @PutMapping("/{id}/cancel")
    public Result<String> cancelBooking(@PathVariable Long id, @RequestBody Map<String, String> cancelData) {
        try {
            CultureBooking booking = cultureBookingMapper.selectById(id);
            if (booking == null) {
                return Result.error("预订不存在");
            }
            // 验证是否是当前用户的预订
            Long userId = getCurrentUserId();
            if (!userId.equals(booking.getUserId())) {
                return Result.error("无权操作");
            }
            if (booking.getStatus() == 4) {
                return Result.error("预订已取消");
            }
            if (booking.getStatus() == 3) {
                return Result.error("预订已完成，无法取消");
            }
            
            booking.setStatus(4); // 4-已取消
            booking.setCancelTime(LocalDateTime.now());
            booking.setCancelReason(cancelData.get("reason"));
            booking.setUpdateTime(LocalDateTime.now());
            cultureBookingMapper.updateById(booking);
            
            return Result.success("取消成功");
        } catch (Exception e) {
            return Result.error("取消失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成预订编号
     */
    private String generateBookingNo() {
        return "BK" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
    
    /**
     * 获取当前用户ID（需要从JWT token中获取）
     * TODO: 实现从SecurityContext或JWT中获取用户ID
     */
    private Long getCurrentUserId() {
        // 临时返回1，实际应该从SecurityContext或JWT中获取
        return 1L;
    }
}

