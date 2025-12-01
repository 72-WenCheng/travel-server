package com.zhly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhly.entity.UserBrowseHistory;
import com.zhly.mapper.UserBrowseHistoryMapper;
import com.zhly.service.GeoIpService;
import com.zhly.service.UserBrowseHistoryService;
import com.zhly.service.dto.GeoIpInfo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户浏览历史服务实现类
 */
@Service
public class UserBrowseHistoryServiceImpl extends ServiceImpl<UserBrowseHistoryMapper, UserBrowseHistory> implements UserBrowseHistoryService {
    
    private final GeoIpService geoIpService;
    
    public UserBrowseHistoryServiceImpl(GeoIpService geoIpService) {
        this.geoIpService = geoIpService;
    }
    
    @Override
    public boolean addBrowseHistory(Long userId, Integer browseType, Long browseId, String browseTitle, String browseImage) {
        return addBrowseHistory(userId, browseType, browseId, browseTitle, browseImage, 0, null);
    }
    
    @Override
    public boolean addBrowseHistory(Long userId, Integer browseType, Long browseId, String browseTitle, String browseImage, Integer duration, String ipAddress) {
        UserBrowseHistory history = new UserBrowseHistory();
        history.setUserId(userId);
        history.setBrowseType(browseType);
        history.setBrowseId(browseId);
        history.setBrowseTitle(browseTitle);
        history.setBrowseImage(browseImage);
        history.setDuration(duration != null ? duration : 0);
        history.setIpAddress(ipAddress);
        
        GeoIpInfo geoIpInfo = geoIpService.lookup(ipAddress);
        if (geoIpInfo != null) {
            history.setCountryCode(geoIpInfo.getCountryCode());
            history.setCountryName(geoIpInfo.getCountryName());
        }
        history.setCreateTime(LocalDateTime.now());
        return this.save(history);
    }
    
    @Override
    public boolean updateBrowseDuration(Long userId, Integer browseType, Long browseId, Integer duration) {
        if (userId == null || browseType == null || browseId == null || duration == null || duration < 0) {
            return false;
        }
        
        // 查找最近的浏览记录（同一用户、同一类型、同一对象）
        QueryWrapper<UserBrowseHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("browse_type", browseType)
                .eq("browse_id", browseId)
                .orderByDesc("create_time")
                .last("LIMIT 1");
        
        UserBrowseHistory history = this.getOne(queryWrapper);
        
        if (history != null) {
            // 更新停留时长（累加，因为可能多次更新）
            Integer currentDuration = history.getDuration() != null ? history.getDuration() : 0;
            history.setDuration(currentDuration + duration);
            return this.updateById(history);
        } else {
            // 如果记录不存在，创建新记录
            UserBrowseHistory newHistory = new UserBrowseHistory();
            newHistory.setUserId(userId);
            newHistory.setBrowseType(browseType);
            newHistory.setBrowseId(browseId);
            newHistory.setDuration(duration);
            newHistory.setCreateTime(LocalDateTime.now());
            return this.save(newHistory);
        }
    }
    
    @Override
    public List<UserBrowseHistory> getRecentHistory(Long userId, Integer limit) {
        return baseMapper.selectRecentByUserId(userId, limit);
    }
    
    @Override
    public boolean clearHistory(Long userId) {
        QueryWrapper<UserBrowseHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return this.remove(queryWrapper);
    }
}







