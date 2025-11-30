package com.zhly.controller;

import com.zhly.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 密码生成控制器 - 生成BCrypt哈希
 */
@RestController
@RequestMapping("/api/debug")
public class PasswordGeneratorController {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * 生成BCrypt密码哈希
     */
    @GetMapping("/generate-password")
    public Result<Map<String, Object>> generatePassword(
            @RequestParam(defaultValue = "123456") String password) {
        
        Map<String, Object> result = new HashMap<>();
        result.put("原始密码", password);
        
        // 生成12个哈希值供选择
        List<String> hashes = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            String hash = passwordEncoder.encode(password);
            hashes.add(hash);
            
            // 验证生成的哈希
            boolean matches = passwordEncoder.matches(password, hash);
            if (!matches) {
                return Result.error("生成的哈希验证失败！");
            }
        }
        
        result.put("生成的哈希值", hashes);
        result.put("SQL更新语句", generateUpdateSQL(hashes.get(0)));
        
        return Result.success("密码哈希生成成功", result);
    }
    
    private String generateUpdateSQL(String hash) {
        return String.format(
            "-- 更新所有用户密码为: 123456\n" +
            "UPDATE sys_user SET password = '%s';\n\n" +
            "-- 或者分别更新\n" +
            "UPDATE sys_user SET password = '%s' WHERE username = 'admin';\n" +
            "UPDATE sys_user SET password = '%s' WHERE username = 'user001';\n" +
            "UPDATE sys_user SET password = '%s' WHERE username = 'user002';\n" +
            "-- ... 其他用户",
            hash, hash, hash, hash
        );
    }
}





