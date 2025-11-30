package com.zhly.service.impl;

import com.zhly.entity.AiGenerateLog;
import com.zhly.mapper.AiGenerateLogMapper;
import com.zhly.service.AiService;
import com.zhly.service.AiClientService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI服务实现类
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Service
public class AiServiceImpl extends ServiceImpl<AiGenerateLogMapper, AiGenerateLog> implements AiService {
    
    @Autowired
    private AiGenerateLogMapper aiGenerateLogMapper;
    
    @Autowired
    private AiClientService aiClientService;
    
    @Override
    public Map<String, Object> getLogList(Integer page, Integer size, Long userId, Integer status) {
        try {
            // 计算偏移量
            int offset = (page - 1) * size;
            List<AiGenerateLog> logs = aiGenerateLogMapper.selectLogList(page, size, offset, userId, status);
            Long total = aiGenerateLogMapper.selectLogCount(userId, status);
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", logs);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取AI日志失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getAiStatistics() {
        try {
            return aiGenerateLogMapper.selectAiStatistics();
        } catch (Exception e) {
            throw new RuntimeException("获取AI统计失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getUserAiStats(Long userId) {
        try {
            return aiGenerateLogMapper.selectUserAiStats(userId);
        } catch (Exception e) {
            throw new RuntimeException("获取用户AI统计失败: " + e.getMessage());
        }
    }
    
    @Override
    public String generateContent(Long userId, String request, String modelName) {
        long startTime = System.currentTimeMillis();
        String response = "";
        Integer status = 0;
        Integer tokensUsed = 0;
        Double cost = 0.0;
        
        try {
            // 调用真实的AI客户端生成内容
            response = aiClientService.generateContent(request, modelName, 1000, 0.7);
            
            // 计算响应时间
            long responseTime = System.currentTimeMillis() - startTime;
            
            // 模拟token使用量和成本计算
            tokensUsed = response.length() / 4; // 粗略估算
            cost = tokensUsed * 0.0001; // 模拟成本
            
            status = 1; // 成功
            
            // 记录生成日志
            logAiGenerate(userId, request, response, status, (int) responseTime, modelName, tokensUsed, cost);
            
            return response;
            
        } catch (Exception e) {
            // 记录失败日志
            long responseTime = System.currentTimeMillis() - startTime;
            response = "AI生成失败: " + e.getMessage();
            logAiGenerate(userId, request, response, status, (int) responseTime, modelName, tokensUsed, cost);
            throw new RuntimeException("AI生成失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean logAiGenerate(Long userId, String request, String response, Integer status, Integer responseTime, String modelName, Integer tokensUsed, Double cost) {
        try {
            AiGenerateLog log = new AiGenerateLog();
            log.setUserId(userId);
            log.setRequestContent(request);
            log.setResponseContent(response);
            log.setStatus(status);
            log.setCostTokens(tokensUsed);
            log.setResponseTime(responseTime);  // 保存响应时间
            log.setCreateTime(LocalDateTime.now());
            
            return aiGenerateLogMapper.insert(log) > 0;
        } catch (Exception e) {
            throw new RuntimeException("记录AI生成日志失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean cleanExpiredLogs(Integer days) {
        try {
            return aiGenerateLogMapper.cleanExpiredLogs(days) > 0;
        } catch (Exception e) {
            throw new RuntimeException("清理过期日志失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean deleteLog(Long id) {
        try {
            return aiGenerateLogMapper.deleteById(id) > 0;
        } catch (Exception e) {
            throw new RuntimeException("删除AI日志失败: " + e.getMessage());
        }
    }
}