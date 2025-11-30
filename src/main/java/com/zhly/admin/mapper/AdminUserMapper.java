package com.zhly.admin.mapper;

import com.zhly.admin.entity.AdminUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 管理端用户数据访问层
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {
    
    /**
     * 获取用户列表（带分页和搜索）
     */
    List<AdminUser> selectUserList(@Param("page") Integer page, 
                                   @Param("size") Integer size, 
                                   @Param("keyword") String keyword);
    
    /**
     * 获取用户总数
     */
    Long selectUserCount(@Param("keyword") String keyword);
    
    /**
     * 批量更新用户状态
     */
    int batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") Integer status);
    
    /**
     * 获取用户统计信息
     */
    Object selectUserStatistics();
    
    /**
     * 获取用户列表（用于导出，不分页）
     */
    List<AdminUser> selectUserListForExport(@Param("keyword") String keyword,
                                           @Param("userType") Integer userType,
                                           @Param("role") Integer role,
                                           @Param("status") Integer status);
}



















