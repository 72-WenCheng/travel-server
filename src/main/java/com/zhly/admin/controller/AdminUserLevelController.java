package com.zhly.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhly.common.R;
import com.zhly.entity.UserLevel;
import com.zhly.mapper.UserLevelMapper;
import com.zhly.admin.mapper.AdminUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理端-用户等级管理Controller
 * 
 * @author zhly
 * @since 2025-10-24
 */
@RestController
@RequestMapping("/api/admin/user-level")
public class AdminUserLevelController {
    
    @Autowired
    private UserLevelMapper userLevelMapper;
    
    @Autowired
    private AdminUserMapper adminUserMapper;
    
    /**
     * 获取等级列表
     */
    @GetMapping("/list")
    public R<List<UserLevel>> list() {
        QueryWrapper<UserLevel> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("level_code");
        List<UserLevel> list = userLevelMapper.selectList(wrapper);
        return R.ok(list);
    }
    
    /**
     * 获取等级详情
     */
    @GetMapping("/detail/{id}")
    public R<UserLevel> detail(@PathVariable Long id) {
        UserLevel level = userLevelMapper.selectById(id);
        return R.ok(level);
    }
    
    /**
     * 更新等级配置
     */
    @PutMapping("/update")
    public R<String> update(@RequestBody UserLevel level) {
        level.setUpdateTime(LocalDateTime.now());
        userLevelMapper.updateById(level);
        return R.ok("更新成功");
    }
    
    /**
     * 获取等级统计
     */
    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 统计各等级用户数量（基于sys_user表的level字段）
            QueryWrapper<com.zhly.admin.entity.AdminUser> wrapper = new QueryWrapper<>();
            wrapper.eq("deleted", 0);
            
            // 统计各等级（1-6对应青铜到王者）
            for (int level = 1; level <= 6; level++) {
                QueryWrapper<com.zhly.admin.entity.AdminUser> levelWrapper = new QueryWrapper<>();
                levelWrapper.eq("deleted", 0).eq("level", level);
                Long count = adminUserMapper.selectCount(levelWrapper);
                
                switch (level) {
                    case 1:
                        result.put("bronze", count != null ? count : 0L);
                        break;
                    case 2:
                        result.put("silver", count != null ? count : 0L);
                        break;
                    case 3:
                        result.put("gold", count != null ? count : 0L);
                        break;
                    case 4:
                        result.put("platinum", count != null ? count : 0L);
                        break;
                    case 5:
                        result.put("diamond", count != null ? count : 0L);
                        break;
                    case 6:
                        result.put("king", count != null ? count : 0L);
                        break;
                }
            }
            
            // 总用户数
            Long totalUsers = adminUserMapper.selectCount(wrapper);
            result.put("totalUsers", totalUsers != null ? totalUsers : 0L);
            
            return R.ok(result);
        } catch (Exception e) {
            // 如果查询失败，返回默认值
            Map<String, Object> defaultStats = new HashMap<>();
            defaultStats.put("bronze", 0L);
            defaultStats.put("silver", 0L);
            defaultStats.put("gold", 0L);
            defaultStats.put("platinum", 0L);
            defaultStats.put("diamond", 0L);
            defaultStats.put("king", 0L);
            defaultStats.put("totalUsers", 0L);
            return R.ok(defaultStats);
        }
    }
}







