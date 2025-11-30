package com.zhly.service;

import com.zhly.entity.CultureProject;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.util.Map;

/**
 * 文旅项目服务接口
 * 
 * @author zhly
 * @since 2024-01-01
 */
public interface CultureProjectService extends IService<CultureProject> {
    
    /**
     * 获取文旅项目列表
     */
    Map<String, Object> getProjectList(Integer page, Integer size, String keyword, String region, Integer type, Integer status);
    
    /**
     * 获取文旅项目详情
     */
    CultureProject getProjectById(Long id);
    
    /**
     * 创建文旅项目
     */
    boolean createProject(CultureProject project);
    
    /**
     * 更新文旅项目
     */
    boolean updateProject(CultureProject project);
    
    /**
     * 删除文旅项目
     */
    boolean deleteProject(Long id);
    
    /**
     * 获取热门文旅项目
     */
    List<CultureProject> getHotProjects(Integer limit);
    
    /**
     * 获取地区文旅项目列表
     */
    List<CultureProject> getRegionProjects(String region, Integer limit);
    
    /**
     * 更新文旅项目统计
     */
    boolean updateProjectStats(Long id, String type);
    
    /**
     * 切换文旅项目状态
     */
    boolean toggleProjectStatus(Long id, Integer status);
    
    /**
     * 获取文旅项目统计
     */
    Map<String, Object> getProjectStatistics();
}



















