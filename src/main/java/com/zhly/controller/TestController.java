package com.zhly.controller;

import com.zhly.common.Result;
import com.zhly.entity.User;
import com.zhly.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试控制器 - 用于诊断问题
 * 
 * @author zhly
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * 测试数据库连接
     */
    @GetMapping("/db")
    public Result<Map<String, Object>> testDatabase() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 测试1：查询所有用户数量
            Long totalCount = userMapper.selectCount(null);
            result.put("totalUsers", totalCount);
            
            // 测试2：查询user001
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("username", "user001");
            User user001 = userMapper.selectOne(wrapper);
            result.put("user001Exists", user001 != null);
            if (user001 != null) {
                result.put("user001Info", Map.of(
                    "id", user001.getId(),
                    "username", user001.getUsername(),
                    "nickname", user001.getNickname(),
                    "email", user001.getEmail(),
                    "role", user001.getRole(),
                    "status", user001.getStatus()
                ));
            }
            
            // 测试3：查询admin
            QueryWrapper<User> adminWrapper = new QueryWrapper<>();
            adminWrapper.eq("username", "admin");
            User admin = userMapper.selectOne(adminWrapper);
            result.put("adminExists", admin != null);
            if (admin != null) {
                result.put("adminInfo", Map.of(
                    "id", admin.getId(),
                    "username", admin.getUsername(),
                    "nickname", admin.getNickname(),
                    "email", admin.getEmail(),
                    "role", admin.getRole(),
                    "status", admin.getStatus()
                ));
            }
            
            // 测试4：列出前5个用户
            List<User> topUsers = userMapper.selectList(
                new QueryWrapper<User>().last("LIMIT 5")
            );
            result.put("top5Users", topUsers.stream().map(u -> Map.of(
                "id", u.getId(),
                "username", u.getUsername(),
                "nickname", u.getNickname() != null ? u.getNickname() : "N/A",
                "role", u.getRole()
            )).toList());
            
            return Result.success("数据库测试成功", result);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            return Result.error("数据库测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试密码验证
     */
    @GetMapping("/password")
    public Result<Map<String, Object>> testPassword(
            @RequestParam(defaultValue = "user001") String username,
            @RequestParam(defaultValue = "123456") String password) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 查找用户
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("username", username);
            User user = userMapper.selectOne(wrapper);
            
            if (user == null) {
                return Result.error("用户不存在: " + username);
            }
            
            result.put("username", user.getUsername());
            result.put("storedHash", user.getPassword().substring(0, 30) + "...");
            result.put("inputPassword", password);
            
            // 验证密码
            boolean matches = passwordEncoder.matches(password, user.getPassword());
            result.put("passwordMatches", matches);
            
            // 生成新的哈希用于对比
            String newHash = passwordEncoder.encode(password);
            result.put("newHash", newHash.substring(0, 30) + "...");
            result.put("newHashMatches", passwordEncoder.matches(password, newHash));
            
            if (matches) {
                return Result.success("密码验证成功", result);
            } else {
                Result<Map<String, Object>> errorResult = new Result<>(500, "密码验证失败", result);
                return errorResult;
            }
        } catch (Exception e) {
            result.put("error", e.getMessage());
            Result<Map<String, Object>> errorResult = new Result<>(500, "密码测试失败: " + e.getMessage(), result);
            return errorResult;
        }
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("服务运行正常");
    }
}
