package com.zhly.service.impl;

import com.zhly.entity.SearchLog;
import com.zhly.mapper.SearchLogMapper;
import com.zhly.service.SearchService;
import com.zhly.service.AttractionService;
import com.zhly.service.TravelPlanService;
import com.zhly.service.CultureProjectService;
import com.zhly.service.TravelRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.time.LocalDateTime;

/**
 * 搜索服务实现类
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Service
public class SearchServiceImpl implements SearchService {
    
    @Autowired
    private AttractionService attractionService;
    
    @Autowired
    private TravelPlanService travelPlanService;
    
    @Autowired
    private CultureProjectService cultureProjectService;
    
    @Autowired
    private TravelRouteService travelRouteService;
    
    @Autowired
    private SearchLogMapper searchLogMapper;
    
    @Override
    public Map<String, Object> globalSearch(String keyword, Integer page, Integer size) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 搜索景点
            Map<String, Object> attractions = attractionService.getAttractionList(page, size, keyword, null, null, 1, null, null);
            result.put("attractions", attractions);
            
            // 搜索旅游攻略（只搜索已发布且审核通过的）
            Map<String, Object> travelPlans = travelPlanService.getPlanList(
                page, size, keyword, null, 1, 1, null, null, null, null
            );
            result.put("travelPlans", travelPlans);
            
            // 搜索文旅项目
            Map<String, Object> cultureProjects = cultureProjectService.getProjectList(page, size, keyword, null, null, 1);
            result.put("cultureProjects", cultureProjects);
            
            // 搜索旅游路线
            Map<String, Object> travelRoutes = travelRouteService.getRouteList(page, size, keyword, null, null, 1);
            result.put("travelRoutes", travelRoutes);
            
            return result;
        } catch (Exception e) {
            throw new RuntimeException("全局搜索失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> searchAttractions(String keyword, String city, Integer page, Integer size) {
        try {
            return attractionService.getAttractionList(page, size, keyword, city, null, 1, null, null);
        } catch (Exception e) {
            throw new RuntimeException("搜索景点失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> searchTravelPlans(String keyword, String destination, Integer page, Integer size) {
        try {
            // 只搜索已发布且审核通过的攻略
            return travelPlanService.getPlanList(
                page, size, keyword, destination, 1, 1, null, null, null, null
            );
        } catch (Exception e) {
            throw new RuntimeException("搜索旅游攻略失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> searchCultureProjects(String keyword, String region, Integer page, Integer size) {
        try {
            return cultureProjectService.getProjectList(page, size, keyword, region, null, 1);
        } catch (Exception e) {
            throw new RuntimeException("搜索文旅项目失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> searchTravelRoutes(String keyword, String startCity, String endCity, Integer page, Integer size) {
        try {
            return travelRouteService.getRouteList(page, size, keyword, startCity, endCity, 1);
        } catch (Exception e) {
            throw new RuntimeException("搜索旅游路线失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<String> getSearchSuggestions(String keyword, Integer limit) {
        try {
            // 这里可以实现基于历史搜索、热门搜索等的智能建议
            List<String> suggestions = new ArrayList<>();
            suggestions.add(keyword + " 景点");
            suggestions.add(keyword + " 攻略");
            suggestions.add(keyword + " 路线");
            suggestions.add(keyword + " 项目");
            return suggestions.subList(0, Math.min(limit, suggestions.size()));
        } catch (Exception e) {
            throw new RuntimeException("获取搜索建议失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean recordSearchHistory(Long userId, String keyword, String searchType) {
        try {
            // 记录搜索日志到数据库
            SearchLog searchLog = new SearchLog();
            searchLog.setUserId(userId);
            searchLog.setKeyword(keyword);
            searchLog.setSearchType(searchType);
            searchLog.setResultCount(0); // 结果数量可以在搜索后更新
            searchLog.setSearchTime(LocalDateTime.now());
            
            searchLogMapper.insert(searchLog);
            return true;
        } catch (Exception e) {
            // 记录搜索日志失败不应该影响搜索功能，只记录错误
            System.err.println("记录搜索历史失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 记录搜索日志（带结果数量）
     */
    public void recordSearchLog(Long userId, String keyword, String searchType, Integer resultCount) {
        try {
            SearchLog searchLog = new SearchLog();
            searchLog.setUserId(userId);
            searchLog.setKeyword(keyword);
            searchLog.setSearchType(searchType);
            searchLog.setResultCount(resultCount != null ? resultCount : 0);
            searchLog.setSearchTime(LocalDateTime.now());
            
            searchLogMapper.insert(searchLog);
        } catch (Exception e) {
            // 记录搜索日志失败不应该影响搜索功能，只记录错误
            System.err.println("记录搜索日志失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<Map<String, Object>> getHotSearchWords(Integer limit) {
        try {
            // 实现热门搜索词逻辑 - 可以从数据库或Redis中获取
            List<Map<String, Object>> hotWords = new ArrayList<>();
            
            // 模拟热门搜索词数据
            String[] keywords = {"北京", "上海", "杭州", "成都", "西安", "青岛", "厦门", "三亚", "桂林", "丽江"};
            int[] counts = {1000, 800, 600, 500, 400, 350, 300, 250, 200, 150};
            
            for (int i = 0; i < Math.min(keywords.length, limit); i++) {
                Map<String, Object> word = new HashMap<>();
                word.put("keyword", keywords[i]);
                word.put("count", counts[i]);
                word.put("trend", i < 3 ? "up" : "stable"); // 前3个显示上升趋势
                hotWords.add(word);
            }
            
            return hotWords;
        } catch (Exception e) {
            throw new RuntimeException("获取热门搜索词失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<String> getUserSearchHistory(Long userId, Integer limit) {
        try {
            // 实现用户搜索历史逻辑 - 可以从Redis或数据库中获取
            List<String> history = new ArrayList<>();
            
            // 模拟用户搜索历史数据
            String[] searchHistory = {
                "北京旅游", "上海美食", "杭州西湖", "成都火锅", "西安兵马俑", 
                "青岛啤酒", "厦门鼓浪屿", "三亚海滩", "桂林山水", "丽江古城"
            };
            
            // 根据用户ID生成不同的历史记录
            int startIndex = (int) (userId % searchHistory.length);
            for (int i = 0; i < Math.min(limit, searchHistory.length); i++) {
                int index = (startIndex + i) % searchHistory.length;
                history.add(searchHistory[index]);
            }
            
            return history;
        } catch (Exception e) {
            throw new RuntimeException("获取用户搜索历史失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean clearUserSearchHistory(Long userId) {
        try {
            // 实现清除用户搜索历史逻辑
            // 可以从Redis或数据库中删除用户的搜索历史
            System.out.println("清除用户搜索历史: 用户ID=" + userId);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("清除用户搜索历史失败: " + e.getMessage());
        }
    }
}

