package com.zhly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhly.entity.ProjectApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 政府项目申请Mapper
 */
@Mapper
public interface ProjectApplicationMapper extends BaseMapper<ProjectApplication> {
    
    /**
     * 分页查询申请
     */
    Page<ProjectApplication> selectApplicationPage(
            Page<ProjectApplication> page,
            @Param("applicationNo") String applicationNo,
            @Param("applicantType") Integer applicantType,
            @Param("status") Integer status);
    
    /**
     * 查询申请列表用于导出
     */
    List<ProjectApplication> selectApplicationListForExport(
            @Param("applicationNo") String applicationNo,
            @Param("applicantType") Integer applicantType,
            @Param("status") Integer status,
            @Param("projectId") Long projectId);
}

