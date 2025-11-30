package com.zhly.controller;

import com.zhly.common.Result;
import com.zhly.entity.User;
import com.zhly.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 调试控制器 - 诊断登录问题
 */
@RestController
@RequestMapping("/api/debug")
public class DebugController {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * 完整的登录诊断
     */
    @GetMapping("/login-diagnosis")
    public Result<Map<String, Object>> loginDiagnosis() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 测试数据库连接
            try (Connection conn = dataSource.getConnection()) {
                result.put("1.数据库连接", "成功");
                result.put("1.数据库URL", conn.getMetaData().getURL());
                result.put("1.数据库用户", conn.getMetaData().getUserName());
            } catch (Exception e) {
                result.put("1.数据库连接", "失败: " + e.getMessage());
                return Result.success("诊断完成（数据库连接失败）", result);
            }
            
            // 2. 使用JdbcTemplate直接查询
            try {
                String sql = "SELECT COUNT(*) FROM sys_user";
                Long count = jdbcTemplate.queryForObject(sql, Long.class);
                result.put("2.直接SQL查询用户总数", count);
                
                String sql2 = "SELECT COUNT(*) FROM sys_user WHERE username='user001'";
                Long user001Count = jdbcTemplate.queryForObject(sql2, Long.class);
                result.put("2.直接SQL查询user001", user001Count);
            } catch (Exception e) {
                result.put("2.直接SQL查询", "失败: " + e.getMessage());
            }
            
            // 3. 使用MyBatis Plus查询
            try {
                Long totalCount = userMapper.selectCount(null);
                result.put("3.MyBatisPlus查询总数", totalCount);
                
                QueryWrapper<User> wrapper = new QueryWrapper<>();
                wrapper.eq("username", "user001");
                Long user001Count = userMapper.selectCount(wrapper);
                result.put("3.MyBatisPlus查询user001数量", user001Count);
                
                List<User> users = userMapper.selectList(wrapper);
                result.put("3.MyBatisPlus查询user001结果数", users.size());
                
                if (!users.isEmpty()) {
                    User user = users.get(0);
                    result.put("3.user001详情", Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "password前10位", user.getPassword() != null ? user.getPassword().substring(0, 10) : "null",
                        "role", user.getRole(),
                        "status", user.getStatus()
                    ));
                }
            } catch (Exception e) {
                result.put("3.MyBatisPlus查询", "失败: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 4. 测试密码验证
            try {
                String testPassword = "123456";
                String storedHash = "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cyhg9J9KgWMBCTxc0VFOD.xFEvW2W";
                boolean matches = passwordEncoder.matches(testPassword, storedHash);
                result.put("4.密码验证测试", matches ? "成功" : "失败");
                
                // 生成新哈希
                String newHash = passwordEncoder.encode(testPassword);
                result.put("4.新生成的哈希前30位", newHash.substring(0, 30));
                result.put("4.新哈希验证", passwordEncoder.matches(testPassword, newHash) ? "成功" : "失败");
            } catch (Exception e) {
                result.put("4.密码验证", "失败: " + e.getMessage());
            }
            
            // 5. 检查表结构
            try {
                String sql = "SHOW TABLES LIKE 'sys_user'";
                List<Map<String, Object>> tables = jdbcTemplate.queryForList(sql);
                result.put("5.sys_user表是否存在", !tables.isEmpty());
                
                if (!tables.isEmpty()) {
                    String descSql = "DESC sys_user";
                    List<Map<String, Object>> columns = jdbcTemplate.queryForList(descSql);
                    result.put("5.sys_user表字段数", columns.size());
                    result.put("5.sys_user表字段", columns.stream()
                        .map(col -> col.get("Field"))
                        .toList());
                }
            } catch (Exception e) {
                result.put("5.表结构检查", "失败: " + e.getMessage());
            }
            
            return Result.success("诊断完成", result);
        } catch (Exception e) {
            result.put("错误", e.getMessage());
            e.printStackTrace();
            return new Result<>(500, "诊断失败", result);
        }
    }
    
    /**
     * 模拟登录测试
     */
    @PostMapping("/test-login")
    public Result<Map<String, Object>> testLogin(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        String username = request.get("username");
        String password = request.get("password");
        
        try {
            result.put("输入用户名", username);
            result.put("输入密码", password);
            
            // 步骤1：查询用户
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("username", username);
            List<User> users = userMapper.selectList(wrapper);
            
            result.put("查询结果数", users.size());
            
            if (users.isEmpty()) {
                return new Result<>(400, "用户不存在", result);
            }
            
            User user = users.get(0);
            result.put("找到用户", user.getUsername());
            result.put("用户角色", user.getRole());
            result.put("用户状态", user.getStatus());
            
            // 步骤2：验证密码
            boolean matches = passwordEncoder.matches(password, user.getPassword());
            result.put("密码验证", matches ? "成功" : "失败");
            
            if (!matches) {
                result.put("存储的哈希前30位", user.getPassword().substring(0, 30));
                return new Result<>(400, "密码错误", result);
            }
            
            return Result.success("登录测试成功", result);
        } catch (Exception e) {
            result.put("异常", e.getMessage());
            e.printStackTrace();
            return new Result<>(500, "登录测试失败", result);
        }
    }
}





