package com.zhly.service.impl;

import com.zhly.entity.CultureProject;
import com.zhly.mapper.CultureProjectMapper;
import com.zhly.service.CultureProjectService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文旅项目服务实现类
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Service
public class CultureProjectServiceImpl extends ServiceImpl<CultureProjectMapper, CultureProject> implements CultureProjectService {
    
    @Autowired
    private CultureProjectMapper cultureProjectMapper;
    
    @Override
    public Map<String, Object> getProjectList(Integer page, Integer size, String keyword, String region, Integer type, Integer status) {
        try {
            List<CultureProject> projects = cultureProjectMapper.selectProjectList(page, size, keyword, region, type, status);
            Long total = cultureProjectMapper.selectProjectCount(keyword, region, type, status);
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", projects != null ? projects : java.util.Collections.emptyList());
            result.put("total", total != null ? total : 0L);
            result.put("page", page);
            result.put("size", size);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取文旅项目列表失败: " + e.getMessage());
        }
    }
    
    @Override
    public CultureProject getProjectById(Long id) {
        try {
            return cultureProjectMapper.selectById(id);
        } catch (Exception e) {
            throw new RuntimeException("获取文旅项目详情失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean createProject(CultureProject project) {
        try {
            project.setOrderCount(0);
            project.setRevenue(new java.math.BigDecimal("0.00"));
            project.setStatus(1); // 正常状态
            return cultureProjectMapper.insert(project) > 0;
        } catch (Exception e) {
            throw new RuntimeException("创建文旅项目失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean updateProject(CultureProject project) {
        try {
            return cultureProjectMapper.updateById(project) > 0;
        } catch (Exception e) {
            throw new RuntimeException("更新文旅项目失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean deleteProject(Long id) {
        try {
            // 查询项目状态
            CultureProject project = cultureProjectMapper.selectById(id);
            if (project == null) {
                throw new RuntimeException("项目不存在");
            }
            
            // 只有关闭状态（status=0）的项目才能删除
            if (project.getStatus() != 0) {
                throw new RuntimeException("只有关闭状态的项目才能删除");
            }
            
            return cultureProjectMapper.deleteById(id) > 0;
        } catch (Exception e) {
            throw new RuntimeException("删除文旅项目失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<CultureProject> getHotProjects(Integer limit) {
        try {
            return cultureProjectMapper.selectHotProjects(limit);
        } catch (Exception e) {
            throw new RuntimeException("获取热门文旅项目失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<CultureProject> getRegionProjects(String region, Integer limit) {
        try {
            return cultureProjectMapper.selectRegionProjects(region, limit);
        } catch (Exception e) {
            throw new RuntimeException("获取地区文旅项目失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean updateProjectStats(Long id, String type) {
        try {
            CultureProject project = cultureProjectMapper.selectById(id);
            if (project == null) return false;
            
            switch (type) {
                case "order":
                    project.setOrderCount(project.getOrderCount() + 1);
                    break;
                case "revenue":
                    // 这里需要传入具体的收入金额
                    break;
            }
            
            return cultureProjectMapper.updateById(project) > 0;
        } catch (Exception e) {
            throw new RuntimeException("更新文旅项目统计失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean toggleProjectStatus(Long id, Integer status) {
        try {
            CultureProject project = cultureProjectMapper.selectById(id);
            if (project == null) return false;
            
            project.setStatus(status);
            return cultureProjectMapper.updateById(project) > 0;
        } catch (Exception e) {
            throw new RuntimeException("切换文旅项目状态失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getProjectStatistics() {
        try {
            return cultureProjectMapper.selectProjectStatistics();
        } catch (Exception e) {
            throw new RuntimeException("获取文旅项目统计失败: " + e.getMessage());
        }
    }
}









