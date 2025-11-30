package com.zhly.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhly.entity.Banner;

import java.util.List;

/**
 * 轮播图服务接口
 */
public interface BannerService extends IService<Banner> {
    
    /**
     * 获取启用的轮播图列表（用户端）
     */
    List<Banner> getActiveBanners();
    
    /**
     * 根据位置获取启用的轮播图列表
     * @param position 显示位置：HOME-用户平台首页，CULTURE-用户平台文化页面，ADMIN_HOME-管理平台首页
     */
    List<Banner> getActiveBannersByPosition(String position);
    
    /**
     * 增加点击次数
     */
    boolean incrementClickCount(Long id);
    
    /**
     * 分页查询（管理端）
     */
    Page<Banner> pageBanners(int page, int size, String title, Integer status);
    
    /**
     * 获取轮播图统计数据
     */
    java.util.Map<String, Object> getBannerStats();
}







