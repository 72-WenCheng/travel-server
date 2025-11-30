package com.zhly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhly.entity.Banner;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 轮播图Mapper接口
 */
@Mapper
public interface BannerMapper extends BaseMapper<Banner> {
    
    /**
     * 增加点击次数
     */
    @Update("UPDATE banner SET click_count = click_count + 1 WHERE id = #{id}")
    int incrementClickCount(Long id);
    
    /**
     * 获取启用的轮播图列表（按显示顺序排序）
     */
    List<Banner> selectActiveBanners();
    
    /**
     * 根据位置获取启用的轮播图列表（按显示顺序排序）
     * @param position 显示位置
     */
    List<Banner> selectActiveBannersByPosition(String position);
}







