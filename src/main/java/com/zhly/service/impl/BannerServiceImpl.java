package com.zhly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhly.entity.Banner;
import com.zhly.mapper.BannerMapper;
import com.zhly.service.BannerService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 轮播图服务实现类
 */
@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {
    
    @Override
    public List<Banner> getActiveBanners() {
        return baseMapper.selectActiveBanners();
    }
    
    @Override
    public List<Banner> getActiveBannersByPosition(String position) {
        if (StringUtils.hasText(position)) {
            return baseMapper.selectActiveBannersByPosition(position);
        }
        // 如果没有指定位置，返回所有启用的轮播图
        return baseMapper.selectActiveBanners();
    }
    
    @Override
    public boolean incrementClickCount(Long id) {
        return baseMapper.incrementClickCount(id) > 0;
    }
    
    @Override
    public Page<Banner> pageBanners(int page, int size, String title, Integer status) {
        Page<Banner> pageInfo = new Page<>(page, size);
        QueryWrapper<Banner> queryWrapper = new QueryWrapper<>();
        
        if (StringUtils.hasText(title)) {
            queryWrapper.like("title", title);
        }
        
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        
        queryWrapper.orderByAsc("display_order").orderByDesc("create_time");
        
        return this.page(pageInfo, queryWrapper);
    }
    
    @Override
    public Map<String, Object> getBannerStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 总轮播图数
            long total = this.count();
            stats.put("total", total);
            
            // 启用中的轮播图数
            LambdaQueryWrapper<Banner> activeQuery = new LambdaQueryWrapper<>();
            activeQuery.eq(Banner::getStatus, 1);
            long active = this.count(activeQuery);
            stats.put("active", active);
            
            // 总点击量
            List<Banner> allBanners = this.list();
            int totalClicks = allBanners.stream()
                .mapToInt(b -> b.getClickCount() != null ? b.getClickCount() : 0)
                .sum();
            stats.put("totalClicks", totalClicks);
            
            // 平均点击率（这里简化为总点击量/总轮播图数）
            double avgClickRate = total > 0 ? (double) totalClicks / total : 0.0;
            stats.put("avgClickRate", String.format("%.1f", avgClickRate));
            
        } catch (Exception e) {
            // 返回默认值
            stats.put("total", 0L);
            stats.put("active", 0L);
            stats.put("totalClicks", 0);
            stats.put("avgClickRate", "0.0");
        }
        
        return stats;
    }
}







