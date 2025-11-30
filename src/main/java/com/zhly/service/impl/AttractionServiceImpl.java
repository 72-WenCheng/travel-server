package com.zhly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhly.entity.Attraction;
import com.zhly.entity.AttractionRating;
import com.zhly.mapper.AttractionMapper;
import com.zhly.mapper.AttractionRatingMapper;
import com.zhly.service.AttractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 景点服务实现类
 *
 * @author zhly
 * @since 2024-01-01
 */
@Service
public class AttractionServiceImpl extends ServiceImpl<AttractionMapper, Attraction> implements AttractionService {

    @Autowired
    private AttractionMapper attractionMapper;

    @Autowired
    private AttractionRatingMapper attractionRatingMapper;

    @Override
    public Map<String, Object> getAttractionList(Integer page, Integer size, String keyword, String city, Integer type, Integer status, String startDate, String endDate) {
        try {
            System.out.println("AttractionServiceImpl.getAttractionList - 参数: city=" + city + ", status=" + status);
            List<Attraction> attractions = attractionMapper.selectAttractionList(page, size, keyword, city, type, status, startDate, endDate);
            Long total = attractionMapper.selectAttractionCount(keyword, city, type, status, startDate, endDate);
            System.out.println("AttractionServiceImpl.getAttractionList - 查询结果: " + (attractions != null ? attractions.size() : 0) + " 条, 总数: " + total);
            if (attractions != null && !attractions.isEmpty()) {
                System.out.println("第一条景点数据 - city字段值: " + attractions.get(0).getCity());
            }

            Map<String, Object> result = new HashMap<>();
            result.put("list", attractions);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取景点列表失败: " + e.getMessage());
        }
    }

    @Override
    public Attraction getAttractionById(Long id) {
        try {
            // 直接返回景点信息，不自动增加浏览量
            // 浏览量应该由前端显式调用 updateAttractionStats 接口来增加
            Attraction attraction = attractionMapper.selectById(id);
            return attraction;
        } catch (Exception e) {
            throw new RuntimeException("获取景点详情失败: " + e.getMessage());
        }
    }

    @Override
    public boolean createAttraction(Attraction attraction) {
        try {
            attraction.setViewCount(0);
            attraction.setCollectCount(0);
            attraction.setStatus(1); // 正常状态
            return attractionMapper.insert(attraction) > 0;
        } catch (Exception e) {
            throw new RuntimeException("创建景点失败: " + e.getMessage());
        }
    }

    @Override
    public boolean updateAttraction(Attraction attraction) {
        try {
            return attractionMapper.updateById(attraction) > 0;
        } catch (Exception e) {
            throw new RuntimeException("更新景点失败: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteAttraction(Long id) {
        try {
            return attractionMapper.deleteById(id) > 0;
        } catch (Exception e) {
            throw new RuntimeException("删除景点失败: " + e.getMessage());
        }
    }

    @Override
    public List<Attraction> getHotAttractions(Integer limit) {
        try {
            return attractionMapper.selectHotAttractions(limit);
        } catch (Exception e) {
            throw new RuntimeException("获取热门景点失败: " + e.getMessage());
        }
    }

    @Override
    public List<Attraction> getCityAttractions(String city, Integer limit) {
        try {
            return attractionMapper.selectCityAttractions(city, limit);
        } catch (Exception e) {
            throw new RuntimeException("获取城市景点失败: " + e.getMessage());
        }
    }

    @Override
    public boolean updateAttractionStats(Long id, String type) {
        try {
            return attractionMapper.updateAttractionStats(id, type) > 0;
        } catch (Exception e) {
            throw new RuntimeException("更新景点统计失败: " + e.getMessage());
        }
    }

    @Override
    public boolean toggleAttractionStatus(Long id, Integer status) {
        try {
            Attraction attraction = attractionMapper.selectById(id);
            if (attraction == null) return false;

            attraction.setStatus(status);
            return attractionMapper.updateById(attraction) > 0;
        } catch (Exception e) {
            throw new RuntimeException("切换景点状态失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> submitRating(Long attractionId, Long userId, Integer score) {
        try {
            if (attractionId == null) {
                throw new RuntimeException("缺少景点ID");
            }
            if (userId == null) {
                throw new RuntimeException("用户未登录");
            }
            if (score == null || score < 1 || score > 5) {
                throw new RuntimeException("评分必须在1-5之间");
            }

            Attraction attraction = attractionMapper.selectById(attractionId);
            if (attraction == null || (attraction.getDeleted() != null && attraction.getDeleted() == 1)) {
                throw new RuntimeException("景点不存在或已删除");
            }

            QueryWrapper<AttractionRating> wrapper = new QueryWrapper<>();
            wrapper.eq("attraction_id", attractionId)
                   .eq("user_id", userId);
            AttractionRating ratingRecord = attractionRatingMapper.selectOne(wrapper);
            LocalDateTime now = LocalDateTime.now();
            if (ratingRecord == null) {
                ratingRecord = new AttractionRating();
                ratingRecord.setAttractionId(attractionId);
                ratingRecord.setUserId(userId);
                ratingRecord.setScore(score);
                ratingRecord.setCreateTime(now);
                ratingRecord.setUpdateTime(now);
                attractionRatingMapper.insert(ratingRecord);
            } else {
                ratingRecord.setScore(score);
                ratingRecord.setUpdateTime(now);
                attractionRatingMapper.updateById(ratingRecord);
            }

            RatingStats stats = calculateRatingStats(attractionId, userId);
            // 同步更新景点的评分信息（score保留小数，rating为四舍五入后的整数）
            LambdaUpdateWrapper<Attraction> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Attraction::getId, attractionId)
                    .set(Attraction::getScore, BigDecimal.valueOf(stats.avgScore))
                    .set(Attraction::getRating, (int) Math.round(stats.avgScore));
            attractionMapper.update(null, updateWrapper);

            Map<String, Object> result = new HashMap<>();
            result.put("averageScore", Math.round(stats.avgScore * 10.0) / 10.0);
            result.put("ratingCount", stats.totalCount);
            result.put("userScore", score);
            return result;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("提交评分失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getRatingSummary(Long attractionId, Long userId) {
        try {
            if (attractionId == null) {
                throw new RuntimeException("缺少景点ID");
            }
            RatingStats stats = calculateRatingStats(attractionId, userId);
            Map<String, Object> result = new HashMap<>();
            result.put("averageScore", Math.round(stats.avgScore * 10.0) / 10.0);
            result.put("ratingCount", stats.totalCount);
            result.put("userScore", stats.userScore);
            return result;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("获取评分信息失败: " + e.getMessage());
        }
    }

    /**
     * 计算景点评分统计信息
     */
    private RatingStats calculateRatingStats(Long attractionId, Long userId) {
        RatingStats stats = new RatingStats();
        QueryWrapper<AttractionRating> summaryWrapper = new QueryWrapper<>();
        summaryWrapper.select("AVG(score) as avgScore", "COUNT(*) as totalCount");
        summaryWrapper.eq("attraction_id", attractionId);
        List<Map<String, Object>> summaryList = attractionRatingMapper.selectMaps(summaryWrapper);
        if (summaryList != null && !summaryList.isEmpty()) {
            Map<String, Object> row = summaryList.get(0);
            if (row.get("avgScore") != null) {
                stats.avgScore = ((Number) row.get("avgScore")).doubleValue();
            }
            if (row.get("totalCount") != null) {
                stats.totalCount = ((Number) row.get("totalCount")).longValue();
            }
        }

        if (userId != null) {
            QueryWrapper<AttractionRating> userWrapper = new QueryWrapper<>();
            userWrapper.eq("attraction_id", attractionId)
                       .eq("user_id", userId);
            AttractionRating userRating = attractionRatingMapper.selectOne(userWrapper);
            if (userRating != null) {
                stats.userScore = userRating.getScore();
            }
        }
        return stats;
    }

    private static class RatingStats {
        double avgScore = 0.0;
        long totalCount = 0L;
        Integer userScore;
    }
}



