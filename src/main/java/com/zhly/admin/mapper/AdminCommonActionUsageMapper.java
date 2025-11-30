package com.zhly.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhly.admin.entity.AdminCommonActionUsage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 管理员常用操作统计 Mapper
 */
@Mapper
public interface AdminCommonActionUsageMapper extends BaseMapper<AdminCommonActionUsage> {

    /**
     * 记录或更新一次点击
     */
    int upsertUsage(AdminCommonActionUsage usage);

    /**
     * 查询管理员的常用操作列表
     */
    List<AdminCommonActionUsage> selectTopActions(@Param("adminId") Long adminId,
                                                  @Param("limit") Integer limit);
}


