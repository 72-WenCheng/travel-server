package com.zhly.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhly.common.Result;
import com.zhly.entity.Coupon;
import com.zhly.mapper.CouponMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 管理端-优惠券管理控制器
 */
@Tag(name = "管理端-优惠券管理")
@RestController
@RequestMapping("/api/admin/culture/coupon")
@RequiredArgsConstructor
public class AdminCouponController {
    
    private final CouponMapper couponMapper;
    
    @Operation(summary = "分页查询优惠券")
    @GetMapping("/page")
    public Result<Page<Coupon>> pageCoupons(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status) {
        try {
            Page<Coupon> pageParam = new Page<>(page, size);
            QueryWrapper<Coupon> wrapper = new QueryWrapper<>();
            if (name != null && !name.isEmpty()) {
                wrapper.like("name", name);
            }
            if (type != null) {
                wrapper.eq("type", type);
            }
            if (status != null) {
                wrapper.eq("status", status);
            }
            wrapper.orderByDesc("create_time");
            Page<Coupon> result = couponMapper.selectPage(pageParam, wrapper);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询优惠券失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "获取优惠券详情")
    @GetMapping("/{id}")
    public Result<Coupon> getCoupon(@PathVariable Long id) {
        try {
            Coupon coupon = couponMapper.selectById(id);
            if (coupon == null) {
                return Result.error("优惠券不存在");
            }
            return Result.success(coupon);
        } catch (Exception e) {
            return Result.error("获取优惠券详情失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "新增优惠券")
    @PostMapping
    public Result<String> createCoupon(@RequestBody Coupon coupon) {
        try {
            coupon.setRemainingCount(coupon.getTotalCount());
            coupon.setCreateTime(LocalDateTime.now());
            coupon.setUpdateTime(LocalDateTime.now());
            couponMapper.insert(coupon);
            return Result.success("新增成功");
        } catch (Exception e) {
            return Result.error("新增失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "更新优惠券")
    @PutMapping("/{id}")
    public Result<String> updateCoupon(@PathVariable Long id, @RequestBody Coupon coupon) {
        try {
            Coupon existing = couponMapper.selectById(id);
            if (existing == null) {
                return Result.error("优惠券不存在");
            }
            
            coupon.setId(id);
            coupon.setUpdateTime(LocalDateTime.now());
            // 保持剩余数量不变
            coupon.setRemainingCount(existing.getRemainingCount());
            couponMapper.updateById(coupon);
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "删除优惠券")
    @DeleteMapping("/{id}")
    public Result<String> deleteCoupon(@PathVariable Long id) {
        try {
            Coupon coupon = couponMapper.selectById(id);
            if (coupon == null) {
                return Result.error("优惠券不存在");
            }
            couponMapper.deleteById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "切换优惠券状态")
    @PutMapping("/{id}/status")
    public Result<String> toggleCouponStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> statusInfo) {
        try {
            Coupon coupon = couponMapper.selectById(id);
            if (coupon == null) {
                return Result.error("优惠券不存在");
            }
            
            coupon.setStatus(statusInfo.get("status"));
            coupon.setUpdateTime(LocalDateTime.now());
            couponMapper.updateById(coupon);
            return Result.success("操作成功");
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }
}

