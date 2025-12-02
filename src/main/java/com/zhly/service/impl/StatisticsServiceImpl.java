package com.zhly.service.impl;

import com.zhly.service.StatisticsService;
import com.zhly.entity.TravelPlan;
import com.zhly.entity.ProjectApplication;
import com.zhly.entity.Report;
import com.zhly.entity.UserLoginHistory;
import com.zhly.entity.UserDeactivateRequest;
import com.zhly.mapper.UserMapper;
import com.zhly.mapper.TravelPlanMapper;
import com.zhly.mapper.AttractionMapper;
import com.zhly.mapper.CultureProjectMapper;
import com.zhly.mapper.CultureBookingMapper;
import com.zhly.mapper.ProjectApplicationMapper;
import com.zhly.mapper.AiGenerateLogMapper;
import com.zhly.mapper.UserDeactivateRequestMapper;
import com.zhly.service.UserDeactivationManager;
import com.zhly.util.OnlineUserManager;
import com.zhly.mapper.CommentMapper;
import com.zhly.mapper.UserCollectMapper;
import com.zhly.mapper.UserLikeMapper;
import com.zhly.mapper.OrderMapper;
import com.zhly.mapper.UserBrowseHistoryMapper;
import com.zhly.mapper.ReportMapper;
import com.zhly.mapper.UserLoginHistoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 数据统计服务实现类
 *
 * @author zhly
 * @since 2024-01-01
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TravelPlanMapper travelPlanMapper;

    @Autowired
    private AttractionMapper attractionMapper;

    @Autowired
    private CultureProjectMapper cultureProjectMapper;

    @Autowired
    private CultureBookingMapper cultureBookingMapper;

    @Autowired
    private AiGenerateLogMapper aiGenerateLogMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserCollectMapper userCollectMapper;

    @Autowired
    private UserLikeMapper userLikeMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserBrowseHistoryMapper browseHistoryMapper;

    @Autowired
    private UserLoginHistoryMapper userLoginHistoryMapper;

    @Autowired
    private OnlineUserManager onlineUserManager;

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private ProjectApplicationMapper projectApplicationMapper;

    @Autowired
    private UserDeactivateRequestMapper userDeactivateRequestMapper;

    private static final int REPORT_TYPE_COMMENT = 3;
    private static final int REPORT_STATUS_PENDING = 0;
    private static final int REPORT_STATUS_RESOLVED = 2;
    private static final int REPORT_STATUS_REJECTED = 3;

    @Override
    public Map<String, Object> getSystemOverview() {
        try {
            Map<String, Object> result = new HashMap<>();
            LocalDate today = LocalDate.now();
            LocalDateTime todayStart = today.atStartOfDay();
            LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();
            LocalDate firstDayOfMonth = today.withDayOfMonth(1);
            LocalDateTime monthStart = firstDayOfMonth.atStartOfDay();

            // ========== 用户统计 ==========
            Map<String, Object> userStats = userMapper.selectUserStatistics();
            Long totalUsers = userStats != null && userStats.get("total") != null ?
                    ((Number) userStats.get("total")).longValue() : 0L;

            // 今日新增用户
            QueryWrapper<com.zhly.entity.User> userTodayWrapper = new QueryWrapper<>();
            userTodayWrapper.ge("create_time", todayStart);
            userTodayWrapper.lt("create_time", todayEnd);
            Long todayNewUsers = userMapper.selectCount(userTodayWrapper);

            // 本月新增用户
            QueryWrapper<com.zhly.entity.User> userMonthWrapper = new QueryWrapper<>();
            userMonthWrapper.ge("create_time", monthStart);
            Long monthlyUsers = userMapper.selectCount(userMonthWrapper);

            // 昨日新增用户（用于计算增长率）
            LocalDate yesterday = today.minusDays(1);
            LocalDateTime yesterdayStart = yesterday.atStartOfDay();
            LocalDateTime yesterdayEnd = yesterday.plusDays(1).atStartOfDay();
            QueryWrapper<com.zhly.entity.User> userYesterdayWrapper = new QueryWrapper<>();
            userYesterdayWrapper.ge("create_time", yesterdayStart);
            userYesterdayWrapper.lt("create_time", yesterdayEnd);
            Long yesterdayUsers = userMapper.selectCount(userYesterdayWrapper);

            // 用户增长率
            double userGrowthRate = calculateBoundedGrowth(todayNewUsers, yesterdayUsers);

            // 今日访问（按登录用户去重）
            Long todayVisits = 0L;
            QueryWrapper<UserLoginHistory> todayVisitWrapper = new QueryWrapper<>();
            todayVisitWrapper.select("COUNT(DISTINCT user_id) as visitUsers");
            todayVisitWrapper.ge("login_time", todayStart);
            todayVisitWrapper.lt("login_time", todayEnd);
            List<Map<String, Object>> visitUsers = userLoginHistoryMapper.selectMaps(todayVisitWrapper);
            if (visitUsers != null && !visitUsers.isEmpty()) {
                Object visitValue = visitUsers.get(0).get("visitUsers");
                if (visitValue != null) {
                    todayVisits = ((Number) visitValue).longValue();
                }
            }

            // 最近7天活跃用户（按最后登录时间）
            LocalDateTime activeStart = LocalDateTime.now().minusDays(7);
            QueryWrapper<com.zhly.entity.User> activeUserWrapper = new QueryWrapper<>();
            activeUserWrapper.eq("status", 1)
                    .eq("deleted", 0)
                    .ge("last_login_time", activeStart);
            Long activeUsers = userMapper.selectCount(activeUserWrapper);

            QueryWrapper<UserDeactivateRequest> lostUserWrapper = new QueryWrapper<>();
            lostUserWrapper.eq("status", UserDeactivationManager.STATUS_COMPLETED)
                    .ge("complete_time", activeStart);
            Long lostUsers = userDeactivateRequestMapper.selectCount(lostUserWrapper);

            double activeRate = totalUsers > 0 ?
                    ((double) activeUsers / totalUsers * 100) : 0.0;
            double newRate = totalUsers > 0 ?
                    ((double) todayNewUsers / totalUsers * 100) : 0.0;
            double lostRate = (totalUsers + lostUsers) > 0 ?
                    ((double) lostUsers / (totalUsers + lostUsers) * 100) : 0.0;

            result.put("totalUsers", totalUsers);
            result.put("todayUsers", todayNewUsers);
            result.put("monthlyUsers", monthlyUsers);
            result.put("activeUsers", activeUsers);
            // 在线用户数：基于在线用户管理器的实时会话统计
            long onlineUsers = onlineUserManager.getOnlineUserCount();
            result.put("onlineUsers", onlineUsers);
            result.put("userGrowthRate", Math.round(userGrowthRate * 100.0) / 100.0);
            result.put("todayVisits", todayVisits);
            result.put("todayNew", todayNewUsers);
            result.put("activeRate", Math.round(activeRate * 100.0) / 100.0);
            result.put("newRate", Math.round(newRate * 100.0) / 100.0);
            result.put("lostUsers", lostUsers);
            result.put("lostRate", Math.round(lostRate * 100.0) / 100.0);

            // ========== 攻略统计 ==========
            Long totalPlans = travelPlanMapper.selectPlanCount(null, null, null);

            // 今日发布攻略（按 publish_time 统计，只统计已发布且审核通过的）
            QueryWrapper<TravelPlan> planTodayWrapper = new QueryWrapper<>();
            planTodayWrapper.eq("status", 1);
            planTodayWrapper.eq("audit_status", 1);
            planTodayWrapper.ge("publish_time", todayStart);
            planTodayWrapper.lt("publish_time", todayEnd);
            Long todayPlans = travelPlanMapper.selectCount(planTodayWrapper);

            // 本月发布攻略（按 publish_time 统计，只统计已发布且审核通过的）
            QueryWrapper<TravelPlan> planMonthWrapper = new QueryWrapper<>();
            planMonthWrapper.eq("status", 1);
            planMonthWrapper.eq("audit_status", 1);
            planMonthWrapper.ge("publish_time", monthStart);
            Long monthlyPlans = travelPlanMapper.selectCount(planMonthWrapper);

            // 待审核攻略（audit_status=0）
            QueryWrapper<TravelPlan> planPendingWrapper = new QueryWrapper<>();
            planPendingWrapper.eq("audit_status", 0);
            Long pendingPlans = travelPlanMapper.selectCount(planPendingWrapper);

            // 平均浏览量
            QueryWrapper<TravelPlan> planViewsWrapper = new QueryWrapper<>();
            planViewsWrapper.select("AVG(view_count) as avgViews");
            List<Map<String, Object>> avgViewsList = travelPlanMapper.selectMaps(planViewsWrapper);
            double avgPlanViews = 0.0;
            if (avgViewsList != null && !avgViewsList.isEmpty()) {
                Map<String, Object> firstRow = avgViewsList.get(0);
                if (firstRow != null && firstRow.get("avgViews") != null) {
                    avgPlanViews = ((Number) firstRow.get("avgViews")).doubleValue();
                }
            }

            // 昨日发布攻略（用于计算增长率，按 publish_time）
            QueryWrapper<TravelPlan> planYesterdayWrapper = new QueryWrapper<>();
            planYesterdayWrapper.eq("status", 1);
            planYesterdayWrapper.eq("audit_status", 1);
            planYesterdayWrapper.ge("publish_time", yesterdayStart);
            planYesterdayWrapper.lt("publish_time", yesterdayEnd);
            Long yesterdayPlans = travelPlanMapper.selectCount(planYesterdayWrapper);

            // 攻略增长率
            double planGrowthRate = calculateBoundedGrowth(todayPlans, yesterdayPlans);

            result.put("totalPlans", totalPlans);
            result.put("todayPlans", todayPlans);
            result.put("monthlyPlans", monthlyPlans);
            result.put("pendingPlans", pendingPlans);
            result.put("avgPlanViews", Math.round(avgPlanViews * 100.0) / 100.0);
            result.put("planGrowthRate", Math.round(planGrowthRate * 100.0) / 100.0);

            // ========== 景点统计 ==========
            Map<String, Object> attractionStats = attractionMapper.selectAttractionStatistics();
            Long totalAttractions = attractionStats != null && attractionStats.get("total") != null ?
                    ((Number) attractionStats.get("total")).longValue() : 0L;

            // 已上架景点（status=1）
            QueryWrapper<com.zhly.entity.Attraction> attractionPublishedWrapper = new QueryWrapper<>();
            attractionPublishedWrapper.eq("status", 1);
            attractionPublishedWrapper.eq("deleted", 0);
            Long publishedAttractions = attractionMapper.selectCount(attractionPublishedWrapper);

            // 已下架景点（status=0或deleted=1）
            QueryWrapper<com.zhly.entity.Attraction> attractionUnpublishedWrapper = new QueryWrapper<>();
            attractionUnpublishedWrapper.and(wrapper -> wrapper.eq("status", 0).or().eq("deleted", 1));
            Long unpublishedAttractions = attractionMapper.selectCount(attractionUnpublishedWrapper);

            // 平均评分（优先使用score，没有则使用rating）
            QueryWrapper<com.zhly.entity.Attraction> attractionRatingWrapper = new QueryWrapper<>();
            attractionRatingWrapper.select("AVG(CASE WHEN score IS NOT NULL THEN score ELSE rating END) as avgRating");
            attractionRatingWrapper.eq("status", 1);
            attractionRatingWrapper.eq("deleted", 0);
            List<Map<String, Object>> avgRatingList = attractionMapper.selectMaps(attractionRatingWrapper);
            double avgRating = 0.0;
            if (avgRatingList != null && !avgRatingList.isEmpty()) {
                Map<String, Object> firstRow = avgRatingList.get(0);
                if (firstRow != null && firstRow.get("avgRating") != null) {
                    avgRating = ((Number) firstRow.get("avgRating")).doubleValue();
                }
            }

            // 总浏览量
            QueryWrapper<com.zhly.entity.Attraction> attractionViewsWrapper = new QueryWrapper<>();
            attractionViewsWrapper.select("SUM(view_count) as totalViews");
            attractionViewsWrapper.eq("status", 1);
            attractionViewsWrapper.eq("deleted", 0);
            List<Map<String, Object>> totalViewsList = attractionMapper.selectMaps(attractionViewsWrapper);
            Long totalAttractionViews = 0L;
            if (totalViewsList != null && !totalViewsList.isEmpty()) {
                Map<String, Object> firstRow = totalViewsList.get(0);
                if (firstRow != null && firstRow.get("totalViews") != null) {
                    totalAttractionViews = ((Number) firstRow.get("totalViews")).longValue();
                }
            }

            // 上架率
            double publishRate = totalAttractions > 0 ?
                    ((double) publishedAttractions / totalAttractions * 100) : 0.0;
            publishRate = Math.min(100.0, Math.max(0.0, publishRate));

            result.put("totalAttractions", totalAttractions);
            result.put("publishedAttractions", publishedAttractions);
            result.put("unpublishedAttractions", unpublishedAttractions);
            result.put("avgRating", Math.round(avgRating * 10.0) / 10.0);
            result.put("totalAttractionViews", totalAttractionViews);
            result.put("publishRate", Math.round(publishRate * 100.0) / 100.0);

            // ========== 订单统计 ==========
            Map<String, Object> orderStats = orderMapper.selectOrderStatistics();
            Long totalOrders = orderStats != null && orderStats.get("total") != null ?
                    ((Number) orderStats.get("total")).longValue() : 0L;

            // 今日订单
            QueryWrapper<com.zhly.entity.Order> orderTodayWrapper = new QueryWrapper<>();
            orderTodayWrapper.ge("create_time", todayStart);
            orderTodayWrapper.lt("create_time", todayEnd);
            Long todayOrders = orderMapper.selectCount(orderTodayWrapper);

            // 本月订单
            QueryWrapper<com.zhly.entity.Order> orderMonthWrapper = new QueryWrapper<>();
            orderMonthWrapper.ge("create_time", monthStart);
            Long monthlyOrders = orderMapper.selectCount(orderMonthWrapper);

            // 待支付订单（order_status=0）
            QueryWrapper<com.zhly.entity.Order> orderPendingWrapper = new QueryWrapper<>();
            orderPendingWrapper.eq("order_status", 0);
            Long pendingOrders = orderMapper.selectCount(orderPendingWrapper);

            // 订单总金额
            QueryWrapper<com.zhly.entity.Order> orderAmountWrapper = new QueryWrapper<>();
            orderAmountWrapper.select("SUM(total_amount) as totalAmount");
            orderAmountWrapper.eq("order_status", 1); // 已支付
            List<Map<String, Object>> totalAmountList = orderMapper.selectMaps(orderAmountWrapper);
            BigDecimal totalAmount = BigDecimal.ZERO;
            if (totalAmountList != null && !totalAmountList.isEmpty()) {
                Map<String, Object> firstRow = totalAmountList.get(0);
                if (firstRow != null && firstRow.get("totalAmount") != null) {
                    totalAmount = new BigDecimal(firstRow.get("totalAmount").toString());
                }
            }

            // 转化率（已支付订单数 / 总订单数）
            QueryWrapper<com.zhly.entity.Order> paidOrderWrapper = new QueryWrapper<>();
            paidOrderWrapper.eq("order_status", 1);
            Long paidOrders = orderMapper.selectCount(paidOrderWrapper);
            double conversionRate = totalOrders > 0 ?
                    ((double) paidOrders / totalOrders * 100) : 0.0;
            conversionRate = Math.min(100.0, Math.max(0.0, conversionRate));

            result.put("totalOrders", totalOrders);
            result.put("todayOrders", todayOrders);
            result.put("monthlyOrders", monthlyOrders);
            result.put("pendingOrders", pendingOrders);
            result.put("totalAmount", totalAmount.doubleValue());
            result.put("conversionRate", Math.round(conversionRate * 100.0) / 100.0);

            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取系统概览统计失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getUserStatistics() {
        try {
            return userMapper.selectUserStatistics();
        } catch (Exception e) {
            throw new RuntimeException("获取用户统计失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getAttractionStatistics() {
        try {
            return attractionMapper.selectAttractionStatistics();
        } catch (Exception e) {
            throw new RuntimeException("获取景点统计失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getTravelPlanStatistics() {
        try {
            Map<String, Object> result = new HashMap<>();
            Long total = travelPlanMapper.selectPlanCount(null, null, null);
            result.put("total", total != null ? total : 0);
            result.put("published", 0);
            result.put("draft", 0);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取旅游攻略统计失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getCultureProjectStatistics() {
        try {
            return cultureProjectMapper.selectProjectStatistics();
        } catch (Exception e) {
            throw new RuntimeException("获取文旅项目统计失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getOrderStatistics() {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("total", 0);
            result.put("completed", 0);
            result.put("pending", 0);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取订单统计失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getRevenueStatistics() {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("total", 0);
            result.put("monthly", 0);
            result.put("daily", 0);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取收入统计失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getVisitStatistics() {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("today", 0);
            result.put("week", 0);
            result.put("month", 0);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取访问统计失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getPopularContentStatistics() {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("hotPlans", travelPlanMapper.selectHotPlans(10));
            result.put("hotAttractions", java.util.Collections.emptyList());
            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取热门内容统计失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getRegionDistributionStatistics() {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("regions", java.util.Collections.emptyList());
            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取地区分布统计失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<Map<String, Object>> getWorldTrafficStatistics() {
        try {
            List<Map<String, Object>> aggregates = browseHistoryMapper.selectWorldTrafficAggregates();
            if (aggregates == null || aggregates.isEmpty()) {
                return java.util.Collections.emptyList();
            }

            List<Map<String, Object>> list = new java.util.ArrayList<>();
            for (Map<String, Object> row : aggregates) {
                int visits = ((Number) row.getOrDefault("visits", 0)).intValue();
                int queries = ((Number) row.getOrDefault("queries", 0)).intValue();
                Number avgDurationValue = (Number) row.get("avgDuration");
                int avgDurationSeconds = avgDurationValue != null ? avgDurationValue.intValue() : 0;

                String countryCode = (String) row.get("countryCode");
                String cnName = (String) row.getOrDefault("countryName", "未知");
                String enName = resolveEnglishCountryName(countryCode);

                list.add(createWorldTrafficItem(
                        countryCode != null ? countryCode : "UNKNOWN",
                        enName,
                        cnName,
                        visits,
                        queries,
                        avgDurationSeconds
                ));
            }

            list.sort((a, b) -> {
                int v1 = ((Number) b.getOrDefault("visits", 0)).intValue();
                int v2 = ((Number) a.getOrDefault("visits", 0)).intValue();
                return Integer.compare(v1, v2);
            });

            return list;
        } catch (Exception e) {
            throw new RuntimeException("获取全球访问统计失败: " + e.getMessage());
        }
    }
    
    private Map<String, Object> createWorldTrafficItem(String code, String name, String cnName,
                                                       int visits, int queries, int avgDurationSeconds) {
        Map<String, Object> item = new HashMap<>();
        item.put("code", code);
        item.put("name", name);
        item.put("cnName", cnName);
        item.put("visits", visits);
        item.put("queries", queries);
        item.put("avgDuration", avgDurationSeconds);
        return item;
    }

    private String resolveEnglishCountryName(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            return "Unknown";
        }
        try {
            Locale locale = new Locale("", countryCode);
            String name = locale.getDisplayCountry(Locale.ENGLISH);
            return (name == null || name.isBlank()) ? countryCode : name;
        } catch (Exception e) {
            return countryCode;
        }
    }

    @Override
    public Map<String, Object> getTimeTrendStatistics(String type, Integer days) {
        try {
            Map<String, Object> result = new HashMap<>();

            if ("user".equals(type)) {
                int range = (days != null && days > 0) ? days : 7;
                // 获取用户增长趋势 - 最近7天
                List<Map<String, Object>> dailyUsers = userMapper.selectDailyUserGrowth(range);

                // 转换数据格式
                List<String> dates = new java.util.ArrayList<>();
                List<Integer> values = new java.util.ArrayList<>();

                for (Map<String, Object> item : dailyUsers) {
                    dates.add((String) item.get("date"));
                    values.add(((Number) item.get("count")).intValue());
                }

                result.put("dates", dates);
                result.put("values", values);
            } else {
                result.put("dates", java.util.Collections.emptyList());
                result.put("values", java.util.Collections.emptyList());
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取时间趋势统计失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getUserBehaviorStatistics() {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("loginCount", 0);
            result.put("activeCount", 0);
            result.put("averageSessionTime", 0);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取用户行为统计失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getContentQualityStatistics() {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("averageRating", 0);
            result.put("totalComments", 0);
            result.put("totalLikes", 0);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取内容质量统计失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getSystemPerformanceStatistics() {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("avgResponseTime", 0);
            result.put("totalRequests", 0);
            result.put("errorRate", 0);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取系统性能统计失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] exportStatistics(String type, String format) {
        try {
            // 目前仅支持导出概览统计为 CSV
            if (type == null || type.isEmpty()) {
                type = "overview";
            }
            if (format == null || format.isEmpty()) {
                format = "csv";
            }

            Map<String, Object> overview = getSystemOverview();

            StringBuilder sb = new StringBuilder();
            // 表头
            sb.append("key,value\n");

            if (overview != null) {
                for (Map.Entry<String, Object> entry : overview.entrySet()) {
                    sb.append(entry.getKey())
                            .append(',')
                            .append(entry.getValue() == null ? "" : entry.getValue().toString())
                            .append('\n');
                }
            }

            // 添加 BOM，方便在 Excel 中正常显示中文
            byte[] contentBytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
            byte[] result = new byte[bom.length + contentBytes.length];
            System.arraycopy(bom, 0, result, 0, bom.length);
            System.arraycopy(contentBytes, 0, result, bom.length, contentBytes.length);

            return result;
        } catch (Exception e) {
            throw new RuntimeException("导出统计数据失败: " + e.getMessage());
        }
    }

    @Override
    public Object getLatestUsers(Integer limit) {
        try {
            return userMapper.selectLatestUsers(limit);
        } catch (Exception e) {
            throw new RuntimeException("获取最新用户失败: " + e.getMessage());
        }
    }

    @Override
    public Object getLatestPlans(Integer limit) {
        try {
            List<TravelPlan> plans = travelPlanMapper.selectLatestPlans(limit);
            // 为每个攻略设置coverImage（从images字段提取第一张图片）
            for (TravelPlan plan : plans) {
                setCoverImageFromImages(plan);
            }
            return plans;
        } catch (Exception e) {
            throw new RuntimeException("获取最新攻略失败: " + e.getMessage());
        }
    }

    /**
     * 从images字段提取第一张图片作为coverImage
     */
    private void setCoverImageFromImages(TravelPlan plan) {
        if (plan == null) {
            return;
        }

        String images = plan.getImages();
        if (images != null && !images.trim().isEmpty()) {
            // 分割图片字符串（逗号分隔）
            String[] imageArray = images.split(",");
            if (imageArray.length > 0) {
                String firstImage = imageArray[0].trim();
                if (!firstImage.isEmpty()) {
                    plan.setCoverImage(firstImage);
                    return;
                }
            }
        }

        // 如果没有图片，设置为null
        plan.setCoverImage(null);
    }

    @Override
    public Map<String, Object> getFunctionUsageStatistics(String timeRange) {
        try {
            FunctionUsageWindow window = resolveFunctionUsageWindow(timeRange);
            Map<String, Object> result = new HashMap<>();
            result.put("range", window.getRangeKey());
            result.put("rangeLabel", window.getRangeLabel());
            result.put("rangeDays", window.getDurationDays());

            // 1. 内容创作：近期开启/发布的攻略数量
            long contentCurrent = countTravelPlans(window.getCurrentStart(), window.getCurrentEnd());
            long contentPrevious = countTravelPlans(window.getPreviousStart(), window.getPreviousEnd());
            appendUsageMetric(result, "content", contentCurrent, contentPrevious);

            // 2. AI智能服务：AI生成请求次数
            long aiCurrent = countAiRequests(window.getCurrentStart(), window.getCurrentEnd());
            long aiPrevious = countAiRequests(window.getPreviousStart(), window.getPreviousEnd());
            appendUsageMetric(result, "aiService", aiCurrent, aiPrevious);

            // 3. 景点浏览：用户浏览景点（browse_type=1）的次数
            long attractionCurrent = countBrowseByTypes(Arrays.asList(1), window.getCurrentStart(), window.getCurrentEnd());
            long attractionPrevious = countBrowseByTypes(Arrays.asList(1), window.getPreviousStart(), window.getPreviousEnd());
            appendUsageMetric(result, "attraction", attractionCurrent, attractionPrevious);

            // 4. 社区互动：评论 + 点赞 + 收藏
            long interactionCurrent = countInteractions(window.getCurrentStart(), window.getCurrentEnd());
            long interactionPrevious = countInteractions(window.getPreviousStart(), window.getPreviousEnd());
            appendUsageMetric(result, "interaction", interactionCurrent, interactionPrevious);

            // 5. 文旅对接：文旅浏览 (type=3/4) + 预订 + 项目申请
            long cultureCurrent = countCultureUsage(window.getCurrentStart(), window.getCurrentEnd());
            long culturePrevious = countCultureUsage(window.getPreviousStart(), window.getPreviousEnd());
            appendUsageMetric(result, "culture", cultureCurrent, culturePrevious);

            // 6. 订单交易：订单创建数量
            long orderCurrent = countOrders(window.getCurrentStart(), window.getCurrentEnd());
            long orderPrevious = countOrders(window.getPreviousStart(), window.getPreviousEnd());
            appendUsageMetric(result, "order", orderCurrent, orderPrevious);

            // 7. 用户画像：产生行为的去重用户数
            long portraitCurrent = countPortraitUsers(window.getCurrentStart(), window.getCurrentEnd());
            long portraitPrevious = countPortraitUsers(window.getPreviousStart(), window.getPreviousEnd());
            appendUsageMetric(result, "portrait", portraitCurrent, portraitPrevious);

            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取功能使用统计失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getAiDetailStatistics() {
        try {
            Map<String, Object> result = new HashMap<>();

            // 从ai_generate_log表获取真实统计数据
            Map<String, Object> aiStats = aiGenerateLogMapper.selectAiStatistics();

            // 安全地获取数据，避免空指针异常
            long totalRequests = 0;
            double successRate = 0.0;
            long totalTokens = 0;
            long avgResponseTime = 0;

            if (aiStats != null) {
                totalRequests = aiStats.get("totalRequests") != null ?
                        ((Number) aiStats.get("totalRequests")).longValue() : 0;
                successRate = aiStats.get("successRate") != null ?
                        ((Number) aiStats.get("successRate")).doubleValue() : 0.0;
                totalTokens = aiStats.get("totalTokensUsed") != null ?
                        ((Number) aiStats.get("totalTokensUsed")).longValue() : 0;
                avgResponseTime = aiStats.get("avgResponseTime") != null ?
                        ((Number) aiStats.get("avgResponseTime")).longValue() : 0;
            }

            // 计算成本（假设每1000 tokens = 0.01元）
            // 使用BigDecimal确保精确计算
            BigDecimal totalCost = BigDecimal.ZERO;
            if (totalTokens > 0) {
                BigDecimal tokens = new BigDecimal(totalTokens);
                BigDecimal rate = new BigDecimal("0.01"); // 每1000 tokens的成本
                totalCost = tokens.divide(new BigDecimal("1000"), 4, RoundingMode.HALF_UP)
                        .multiply(rate)
                        .setScale(2, RoundingMode.HALF_UP);
            }

            result.put("totalRequests", totalRequests);
            result.put("successRate", successRate);
            result.put("avgResponseTime", avgResponseTime); // 使用数据库计算的平均响应时间
            result.put("totalCost", totalCost.doubleValue()); // 保留2位小数

            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取AI详细统计失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getContentStatistics() {
        try {
            Map<String, Object> result = new HashMap<>();
            LocalDate today = LocalDate.now();
            LocalDateTime todayStart = today.atStartOfDay();
            LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();
            LocalDate yesterday = today.minusDays(1);
            LocalDateTime yesterdayStart = yesterday.atStartOfDay();
            LocalDateTime yesterdayEnd = yesterday.plusDays(1).atStartOfDay();

            // 待处理举报（针对评论）
            QueryWrapper<Report> pendingReportWrapper = new QueryWrapper<>();
            pendingReportWrapper.eq("entity_type", REPORT_TYPE_COMMENT)
                    .eq("status", REPORT_STATUS_PENDING)
                    .eq("deleted", 0);
            Long pendingCommentReports = reportMapper.selectCount(pendingReportWrapper);

            // 举报处理结果统计（用于审核率）
            QueryWrapper<Report> resolvedReportWrapper = new QueryWrapper<>();
            resolvedReportWrapper.eq("entity_type", REPORT_TYPE_COMMENT)
                    .eq("status", REPORT_STATUS_RESOLVED)
                    .eq("deleted", 0);
            Long resolvedReports = reportMapper.selectCount(resolvedReportWrapper);

            QueryWrapper<Report> rejectedReportWrapper = new QueryWrapper<>();
            rejectedReportWrapper.eq("entity_type", REPORT_TYPE_COMMENT)
                    .eq("status", REPORT_STATUS_REJECTED)
                    .eq("deleted", 0);
            Long approvedReports = reportMapper.selectCount(rejectedReportWrapper);

            long processedReports = resolvedReports + approvedReports;
            double commentApprovalRate = processedReports > 0 ?
                    ((double) approvedReports / processedReports * 100) : 0.0;

            // 平均每篇攻略的评论数
            Long totalPlans = travelPlanMapper.selectPlanCount(null, null, null);
            QueryWrapper<com.zhly.entity.Attraction> attractionPublishedWrapper = new QueryWrapper<>();
            attractionPublishedWrapper.eq("status", 1)
                    .eq("deleted", 0);
            Long publishedAttractions = attractionMapper.selectCount(attractionPublishedWrapper);

            QueryWrapper<com.zhly.entity.Comment> planCommentWrapper = new QueryWrapper<>();
            planCommentWrapper.eq("content_type", "PLAN")
                    .eq("status", "PUBLISHED");
            Long planComments = commentMapper.selectCount(planCommentWrapper);

            QueryWrapper<com.zhly.entity.Comment> attractionCommentWrapper = new QueryWrapper<>();
            attractionCommentWrapper.eq("content_type", "ATTRACTION")
                    .eq("status", "PUBLISHED");
            Long attractionComments = commentMapper.selectCount(attractionCommentWrapper);

            QueryWrapper<com.zhly.entity.Comment> planCommentTodayWrapper = new QueryWrapper<>();
            planCommentTodayWrapper.eq("content_type", "PLAN")
                    .eq("status", "PUBLISHED")
                    .ge("created_time", todayStart)
                    .lt("created_time", todayEnd);
            Long todayPlanComments = commentMapper.selectCount(planCommentTodayWrapper);

            QueryWrapper<com.zhly.entity.Comment> attractionCommentTodayWrapper = new QueryWrapper<>();
            attractionCommentTodayWrapper.eq("content_type", "ATTRACTION")
                    .eq("status", "PUBLISHED")
                    .ge("created_time", todayStart)
                    .lt("created_time", todayEnd);
            Long todayAttractionComments = commentMapper.selectCount(attractionCommentTodayWrapper);

            Long totalComments = planComments + attractionComments;
            Long todayComments = todayPlanComments + todayAttractionComments;

            double avgCommentsPerPlan = totalPlans > 0 ?
                    ((double) planComments / totalPlans) : 0.0;
            double avgCommentsPerAttraction = publishedAttractions > 0 ?
                    ((double) attractionComments / publishedAttractions) : 0.0;

            // 昨日评论数（用于计算增长率，仅统计攻略/景点的已发布评论）
            QueryWrapper<com.zhly.entity.Comment> commentYesterdayWrapper = new QueryWrapper<>();
            commentYesterdayWrapper.in("content_type", Arrays.asList("PLAN", "ATTRACTION"))
                    .eq("status", "PUBLISHED")
                    .ge("created_time", yesterdayStart)
                    .lt("created_time", yesterdayEnd);
            Long yesterdayComments = commentMapper.selectCount(commentYesterdayWrapper);

            // 评论增长率
            double commentGrowthRate = calculateBoundedGrowth(todayComments, yesterdayComments);

            result.put("totalComments", totalComments);
            result.put("todayComments", todayComments);
            result.put("pendingComments", pendingCommentReports);
            result.put("commentApprovalRate", Math.round(commentApprovalRate * 100.0) / 100.0);
            result.put("avgCommentsPerPlan", Math.round(avgCommentsPerPlan * 100.0) / 100.0);
            result.put("avgCommentsPerAttraction", Math.round(avgCommentsPerAttraction * 100.0) / 100.0);
            result.put("planComments", planComments);
            result.put("attractionComments", attractionComments);
            result.put("planCommentsToday", todayPlanComments);
            result.put("attractionCommentsToday", todayAttractionComments);
            result.put("commentGrowthRate", Math.round(commentGrowthRate * 100.0) / 100.0);

            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取评论统计失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getInteractionStatistics() {
        try {
            Map<String, Object> result = new HashMap<>();
            LocalDate today = LocalDate.now();
            LocalDateTime todayStart = today.atStartOfDay();
            LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();
            LocalDate yesterday = today.minusDays(1);
            LocalDateTime yesterdayStart = yesterday.atStartOfDay();
            LocalDateTime yesterdayEnd = yesterday.plusDays(1).atStartOfDay();

            // ========== 收藏统计 ==========
            Long totalFavorites = userCollectMapper.selectCount(new QueryWrapper<>());

            // 今日收藏数
            QueryWrapper<com.zhly.entity.UserCollect> collectTodayWrapper = new QueryWrapper<>();
            collectTodayWrapper.ge("create_time", todayStart);
            collectTodayWrapper.lt("create_time", todayEnd);
            Long todayFavorites = userCollectMapper.selectCount(collectTodayWrapper);

            // 攻略收藏数（collect_type=1）
            QueryWrapper<com.zhly.entity.UserCollect> planCollectWrapper = new QueryWrapper<>();
            planCollectWrapper.eq("collect_type", 1);
            Long planFavorites = userCollectMapper.selectCount(planCollectWrapper);

            // 景点收藏数（collect_type=2）
            QueryWrapper<com.zhly.entity.UserCollect> attractionCollectWrapper = new QueryWrapper<>();
            attractionCollectWrapper.eq("collect_type", 2);
            Long attractionFavorites = userCollectMapper.selectCount(attractionCollectWrapper);

            // 收藏转化率（产生收藏的用户 / 总用户）
            Long totalUsers = userMapper.selectCount(new QueryWrapper<>());
            Long favoriteUsers = 0L;
            QueryWrapper<com.zhly.entity.UserCollect> favoriteUserWrapper = new QueryWrapper<>();
            favoriteUserWrapper.select("COUNT(DISTINCT user_id) as userCount");
            List<Map<String, Object>> favoriteUserList = userCollectMapper.selectMaps(favoriteUserWrapper);
            if (favoriteUserList != null && !favoriteUserList.isEmpty()) {
                Map<String, Object> row = favoriteUserList.get(0);
                if (row != null && row.get("userCount") != null) {
                    favoriteUsers = ((Number) row.get("userCount")).longValue();
                }
            }
            double favoriteRate = totalUsers > 0 ?
                    ((double) favoriteUsers / totalUsers * 100) : 0.0;
            favoriteRate = Math.min(100.0, Math.max(0.0, favoriteRate));

            // 昨日收藏数（用于计算增长率）
            QueryWrapper<com.zhly.entity.UserCollect> collectYesterdayWrapper = new QueryWrapper<>();
            collectYesterdayWrapper.ge("create_time", yesterdayStart);
            collectYesterdayWrapper.lt("create_time", yesterdayEnd);
            Long yesterdayFavorites = userCollectMapper.selectCount(collectYesterdayWrapper);

            // 收藏增长率
            double favoriteGrowthRate = calculateBoundedGrowth(todayFavorites, yesterdayFavorites);

            result.put("totalFavorites", totalFavorites);
            result.put("todayFavorites", todayFavorites);
            result.put("planFavorites", planFavorites);
            result.put("attractionFavorites", attractionFavorites);
            result.put("favoriteRate", Math.round(favoriteRate * 100.0) / 100.0);
            result.put("favoriteGrowthRate", Math.round(favoriteGrowthRate * 100.0) / 100.0);

            // ========== 浏览统计 ==========
            Long planViews = 0L;
            QueryWrapper<TravelPlan> planViewSumWrapper = new QueryWrapper<>();
            planViewSumWrapper.select("SUM(view_count) as totalViews");
            planViewSumWrapper.eq("status", 1).eq("audit_status", 1);
            List<Map<String, Object>> planViewSum = travelPlanMapper.selectMaps(planViewSumWrapper);
            if (planViewSum != null && !planViewSum.isEmpty()) {
                Map<String, Object> firstRow = planViewSum.get(0);
                Object totalViewsObj = firstRow != null ? firstRow.get("totalViews") : null;
                if (totalViewsObj != null) {
                    planViews = ((Number) totalViewsObj).longValue();
                }
            }

            Long attractionViews = 0L;
            QueryWrapper<com.zhly.entity.Attraction> attractionViewSumWrapper = new QueryWrapper<>();
            attractionViewSumWrapper.select("SUM(view_count) as totalViews");
            attractionViewSumWrapper.eq("status", 1).eq("deleted", 0);
            List<Map<String, Object>> attractionViewSum = attractionMapper.selectMaps(attractionViewSumWrapper);
            if (attractionViewSum != null && !attractionViewSum.isEmpty()) {
                Map<String, Object> firstRow = attractionViewSum.get(0);
                Object totalViewsObj = firstRow != null ? firstRow.get("totalViews") : null;
                if (totalViewsObj != null) {
                    attractionViews = ((Number) totalViewsObj).longValue();
                }
            }

            Long totalViews = planViews + attractionViews;

            QueryWrapper<com.zhly.entity.UserBrowseHistory> planBrowseTodayWrapper = new QueryWrapper<>();
            planBrowseTodayWrapper.eq("browse_type", 2)
                    .ge("create_time", todayStart)
                    .lt("create_time", todayEnd);
            Long todayPlanViews = browseHistoryMapper.selectCount(planBrowseTodayWrapper);

            QueryWrapper<com.zhly.entity.UserBrowseHistory> attractionBrowseTodayWrapper = new QueryWrapper<>();
            attractionBrowseTodayWrapper.eq("browse_type", 1)
                    .ge("create_time", todayStart)
                    .lt("create_time", todayEnd);
            Long todayAttractionViews = browseHistoryMapper.selectCount(attractionBrowseTodayWrapper);

            Long todayViews = todayPlanViews + todayAttractionViews;

            QueryWrapper<com.zhly.entity.UserBrowseHistory> browseYesterdayWrapper = new QueryWrapper<>();
            browseYesterdayWrapper.in("browse_type", Arrays.asList(1, 2))
                    .ge("create_time", yesterdayStart)
                    .lt("create_time", yesterdayEnd);
            Long yesterdayViews = browseHistoryMapper.selectCount(browseYesterdayWrapper);

            double viewGrowthRate = calculateBoundedGrowth(todayViews, yesterdayViews);

            result.put("totalViews", totalViews);
            result.put("todayViews", todayViews);
            result.put("planViews", planViews);
            result.put("attractionViews", attractionViews);
            result.put("todayPlanViews", todayPlanViews);
            result.put("todayAttractionViews", todayAttractionViews);
            result.put("viewGrowthRate", Math.round(viewGrowthRate * 100.0) / 100.0);

            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取互动统计失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getAiUsageStatistics() {
        try {
            Map<String, Object> result = new HashMap<>();
            LocalDate today = LocalDate.now();
            LocalDateTime todayStart = today.atStartOfDay();
            LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();
            LocalDate yesterday = today.minusDays(1);
            LocalDateTime yesterdayStart = yesterday.atStartOfDay();
            LocalDateTime yesterdayEnd = yesterday.plusDays(1).atStartOfDay();

            // 从AI统计中获取数据
            Map<String, Object> aiStats = aiGenerateLogMapper.selectAiStatistics();
            Long totalRequests = aiStats != null && aiStats.get("totalRequests") != null ?
                    ((Number) aiStats.get("totalRequests")).longValue() : 0L;

            // 今日请求数
            QueryWrapper<com.zhly.entity.AiGenerateLog> aiTodayWrapper = new QueryWrapper<>();
            aiTodayWrapper.ge("create_time", todayStart);
            aiTodayWrapper.lt("create_time", todayEnd);
            Long todayRequests = aiGenerateLogMapper.selectCount(aiTodayWrapper);

            // 成功率
            QueryWrapper<com.zhly.entity.AiGenerateLog> aiSuccessWrapper = new QueryWrapper<>();
            aiSuccessWrapper.eq("status", "SUCCESS");
            Long successCount = aiGenerateLogMapper.selectCount(aiSuccessWrapper);
            double successRate = totalRequests > 0 ?
                    ((double) successCount / totalRequests * 100) : 0.0;

            // 平均响应时间
            QueryWrapper<com.zhly.entity.AiGenerateLog> aiResponseWrapper = new QueryWrapper<>();
            aiResponseWrapper.select("AVG(response_time) as avgResponseTime");
            aiResponseWrapper.eq("status", "SUCCESS");
            List<Map<String, Object>> avgResponseList = aiGenerateLogMapper.selectMaps(aiResponseWrapper);
            double avgResponseTime = 0.0;
            if (avgResponseList != null && !avgResponseList.isEmpty()) {
                Map<String, Object> firstRow = avgResponseList.get(0);
                if (firstRow != null && firstRow.get("avgResponseTime") != null) {
                    avgResponseTime = ((Number) firstRow.get("avgResponseTime")).doubleValue();
                }
            }

            // 使用率（使用AI的用户数 / 总用户数）
            QueryWrapper<com.zhly.entity.AiGenerateLog> aiUserWrapper = new QueryWrapper<>();
            aiUserWrapper.select("COUNT(DISTINCT user_id) as aiUsers");
            List<Map<String, Object>> aiUsersList = aiGenerateLogMapper.selectMaps(aiUserWrapper);
            Long aiUsers = 0L;
            if (aiUsersList != null && !aiUsersList.isEmpty()) {
                Map<String, Object> firstRow = aiUsersList.get(0);
                if (firstRow != null && firstRow.get("aiUsers") != null) {
                    aiUsers = ((Number) firstRow.get("aiUsers")).longValue();
                }
            }
            Long totalUsers = userMapper.selectCount(new QueryWrapper<>());
            double usageRate = totalUsers > 0 ?
                    ((double) aiUsers / totalUsers * 100) : 0.0;

            // 昨日请求数（用于计算增长率）
            QueryWrapper<com.zhly.entity.AiGenerateLog> aiYesterdayWrapper = new QueryWrapper<>();
            aiYesterdayWrapper.ge("create_time", yesterdayStart);
            aiYesterdayWrapper.lt("create_time", yesterdayEnd);
            Long yesterdayRequests = aiGenerateLogMapper.selectCount(aiYesterdayWrapper);

            // 请求增长率
            double requestGrowthRate = calculateBoundedGrowth(todayRequests, yesterdayRequests);

            result.put("totalRequests", totalRequests);
            result.put("todayRequests", todayRequests);
            result.put("successRate", Math.round(successRate * 100.0) / 100.0);
            result.put("avgResponseTime", Math.round(avgResponseTime));
            result.put("usageRate", Math.round(usageRate * 100.0) / 100.0);
            result.put("requestGrowthRate", Math.round(requestGrowthRate * 100.0) / 100.0);

            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取AI使用统计失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getBusinessStatistics() {
        try {
            Map<String, Object> result = new HashMap<>();

            // 订单统计
            Map<String, Object> orderStats = orderMapper.selectOrderStatistics();
            Long totalOrders = orderStats != null && orderStats.get("total") != null ?
                    ((Number) orderStats.get("total")).longValue() : 0L;

            // 待支付订单
            QueryWrapper<com.zhly.entity.Order> orderPendingWrapper = new QueryWrapper<>();
            orderPendingWrapper.eq("order_status", 0);
            Long pendingOrders = orderMapper.selectCount(orderPendingWrapper);

            // 已支付订单
            QueryWrapper<com.zhly.entity.Order> orderPaidWrapper = new QueryWrapper<>();
            orderPaidWrapper.eq("order_status", 1);
            Long paidOrders = orderMapper.selectCount(orderPaidWrapper);

            // 订单总金额
            QueryWrapper<com.zhly.entity.Order> orderAmountWrapper = new QueryWrapper<>();
            orderAmountWrapper.select("SUM(total_amount) as totalAmount");
            orderAmountWrapper.eq("order_status", 1);
            List<Map<String, Object>> totalAmountList = orderMapper.selectMaps(orderAmountWrapper);
            BigDecimal orderAmount = BigDecimal.ZERO;
            if (totalAmountList != null && !totalAmountList.isEmpty()) {
                Map<String, Object> firstRow = totalAmountList.get(0);
                if (firstRow != null && firstRow.get("totalAmount") != null) {
                    orderAmount = new BigDecimal(firstRow.get("totalAmount").toString());
                }
            }

            result.put("totalOrders", totalOrders);
            result.put("pendingOrders", pendingOrders);
            result.put("paidOrders", paidOrders);
            result.put("orderAmount", orderAmount.doubleValue());

            // 优惠券统计（暂时为0，需要优惠券表）
            result.put("issuedCoupons", 0L);
            result.put("usedCoupons", 0L);
            result.put("couponRate", 0.0);

            // 文旅预订统计（暂时为0，需要预订表）
            result.put("cultureBookings", 0L);
            result.put("projectApplications", 0L);
            result.put("pendingReview", 0L);

            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取文旅业务统计失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getPendingStatistics() {
        try {
            Map<String, Object> result = new HashMap<>();

            // 待审核攻略
            QueryWrapper<TravelPlan> planPendingWrapper = new QueryWrapper<>();
            planPendingWrapper.eq("audit_status", 0);
            Long plans = travelPlanMapper.selectCount(planPendingWrapper);

            // 待处理举报（status=0 待处理，deleted=0）
            QueryWrapper<Report> reportPendingWrapper = new QueryWrapper<>();
            reportPendingWrapper.eq("status", 0)
                    .eq("deleted", 0);
            Long reports = reportMapper.selectCount(reportPendingWrapper);

            // 待审核项目申请（status 1-待审核 2-审核中）
            QueryWrapper<ProjectApplication> applicationWrapper = new QueryWrapper<>();
            applicationWrapper.in("status", Arrays.asList(1, 2));
            Long applications = projectApplicationMapper.selectCount(applicationWrapper);

            result.put("plans", plans);
            result.put("reports", reports);
            result.put("applications", applications);

            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取待处理事项统计失败: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getNotifications(Integer limit) {
        try {
            List<Map<String, Object>> notifications = new java.util.ArrayList<>();
            int actualLimit = limit != null && limit > 0 ? limit : 3;

            int fetchSize = actualLimit * 2;

            // 获取最新的订单
            QueryWrapper<com.zhly.entity.Order> orderWrapper = new QueryWrapper<>();
            orderWrapper.orderByDesc("create_time");
            orderWrapper.last("LIMIT " + fetchSize);
            List<com.zhly.entity.Order> recentOrders = orderMapper.selectList(orderWrapper);
            for (com.zhly.entity.Order order : recentOrders) {
                Map<String, Object> notification = new HashMap<>();
                notification.put("id", order.getId());
                notification.put("type", "order");
                notification.put("icon", "ShoppingCart");
                com.zhly.entity.User user = userMapper.selectById(order.getUserId());
                String userName = user != null ? user.getNickname() : "用户";
                notification.put("message", String.format("新订单：%s 购买了 %s", userName, order.getProductName()));
                notification.put("time", formatTimeAgo(order.getCreateTime()));
                notification.put("createTime", order.getCreateTime());
                notifications.add(notification);
            }

            // 获取最新的评论
            QueryWrapper<com.zhly.entity.Comment> commentWrapper = new QueryWrapper<>();
            commentWrapper.eq("status", "PUBLISHED");
            commentWrapper.orderByDesc("created_time");
            commentWrapper.last("LIMIT " + fetchSize);
            List<com.zhly.entity.Comment> recentComments = commentMapper.selectList(commentWrapper);
            for (com.zhly.entity.Comment comment : recentComments) {
                Map<String, Object> notification = new HashMap<>();
                notification.put("id", comment.getId());
                notification.put("type", "comment");
                notification.put("icon", "ChatDotRound");
                com.zhly.entity.User user = userMapper.selectById(comment.getUserId());
                String userName = user != null ? user.getNickname() : "用户";
                String contentTitle = "内容";
                if ("PLAN".equals(comment.getContentType())) {
                    TravelPlan plan = travelPlanMapper.selectById(comment.getContentId());
                    if (plan != null) {
                        contentTitle = plan.getTitle();
                    }
                } else if ("ATTRACTION".equals(comment.getContentType())) {
                    com.zhly.entity.Attraction attraction = attractionMapper.selectById(comment.getContentId());
                    if (attraction != null) {
                        contentTitle = attraction.getName();
                    }
                }
                notification.put("message", String.format("新评论：%s 评论了《%s》", userName, contentTitle));
                notification.put("time", formatTimeAgo(comment.getCreatedTime()));
                notification.put("createTime", comment.getCreatedTime());
                notifications.add(notification);
            }

            // 获取最新发布的攻略
            QueryWrapper<TravelPlan> planWrapper = new QueryWrapper<>();
            planWrapper.eq("status", 1)
                    .eq("audit_status", 1)
                    .orderByDesc("publish_time")
                    .last("LIMIT " + fetchSize);
            List<TravelPlan> recentPlans = travelPlanMapper.selectList(planWrapper);
            for (TravelPlan plan : recentPlans) {
                LocalDateTime publishTime = plan.getPublishTime() != null ? plan.getPublishTime() : plan.getUpdateTime();
                if (publishTime == null) {
                    publishTime = LocalDateTime.now();
                }
                Map<String, Object> notification = new HashMap<>();
                notification.put("id", plan.getId());
                notification.put("type", "plan");
                notification.put("icon", "Document");
                notification.put("message", String.format("新攻略：《%s》已发布", plan.getTitle()));
                notification.put("time", formatTimeAgo(publishTime));
                notification.put("createTime", publishTime);
                notifications.add(notification);
            }

            // 获取最新的举报
            QueryWrapper<Report> reportWrapper = new QueryWrapper<>();
            reportWrapper.eq("deleted", 0)
                    .orderByDesc("create_time")
                    .last("LIMIT " + fetchSize);
            List<Report> recentReports = reportMapper.selectList(reportWrapper);
            for (Report report : recentReports) {
                LocalDateTime reportTime = report.getCreateTime() != null ? report.getCreateTime() : LocalDateTime.now();
                Map<String, Object> notification = new HashMap<>();
                notification.put("id", report.getId());
                notification.put("type", "report");
                notification.put("icon", "Warning");
                notification.put("message", "有新的举报需要处理");
                notification.put("time", formatTimeAgo(reportTime));
                notification.put("createTime", reportTime);
                notifications.add(notification);
            }

            // 获取最新的注销申请
            QueryWrapper<UserDeactivateRequest> deactivateWrapper = new QueryWrapper<>();
            deactivateWrapper.eq("status", 0)
                    .orderByDesc("request_time")
                    .last("LIMIT " + fetchSize);
            List<UserDeactivateRequest> deactivateRequests = userDeactivateRequestMapper.selectList(deactivateWrapper);
            for (UserDeactivateRequest request : deactivateRequests) {
                LocalDateTime requestTime = request.getRequestTime() != null ? request.getRequestTime() : LocalDateTime.now();
                Map<String, Object> notification = new HashMap<>();
                notification.put("id", request.getId());
                notification.put("type", "deactivate");
                notification.put("icon", "Remove");
                com.zhly.entity.User user = userMapper.selectById(request.getUserId());
                String userName = user != null ? user.getNickname() : "用户";
                notification.put("message", String.format("注销申请：%s 申请注销账号", userName));
                notification.put("time", formatTimeAgo(requestTime));
                notification.put("createTime", requestTime);
                notifications.add(notification);
            }

            // 按实际时间倒序排序，取最新的 actualLimit 条
            notifications.sort((a, b) -> {
                LocalDateTime timeA = (LocalDateTime) a.get("createTime");
                LocalDateTime timeB = (LocalDateTime) b.get("createTime");
                if (timeA == null && timeB == null) return 0;
                if (timeA == null) return 1;
                if (timeB == null) return -1;
                return timeB.compareTo(timeA);
            });

            if (notifications.size() > actualLimit) {
                notifications = notifications.subList(0, actualLimit);
            }

            // 移除临时字段
            for (Map<String, Object> notification : notifications) {
                notification.remove("createTime");
            }

            return notifications;
        } catch (Exception e) {
            throw new RuntimeException("获取实时通知失败: " + e.getMessage());
        }
    }

    /**
     * 格式化时间为"X分钟前"、"X小时前"、"X天前"等
     */
    private String formatTimeAgo(LocalDateTime time) {
        if (time == null) {
            return "未知时间";
        }

        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(time, now).toMinutes();

        if (minutes < 1) {
            return "刚刚";
        } else if (minutes < 60) {
            return minutes + "分钟前";
        } else if (minutes < 1440) {
            long hours = minutes / 60;
            return hours + "小时前";
        } else {
            long days = minutes / 1440;
            return days + "天前";
        }
    }

    @Override
    public Map<String, Object> getCommunityStatistics(String period) {
        try {
            Map<String, Object> result = new HashMap<>();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime = null;

            // 根据period设置时间范围
            if ("week".equals(period)) {
                startTime = now.minusDays(7);
            } else if ("month".equals(period)) {
                startTime = now.minusDays(30);
            }

            // 1. 攻略总数
            QueryWrapper<TravelPlan> planWrapper = new QueryWrapper<>();
            planWrapper.eq("status", 1); // 只统计已发布的攻略
            if (startTime != null) {
                planWrapper.ge("create_time", startTime);
            }
            Long totalPlans = travelPlanMapper.selectCount(planWrapper);
            result.put("totalPlans", totalPlans != null ? totalPlans : 0L);

            // 2. 活跃用户（最近7天有登录的用户）
            QueryWrapper<com.zhly.entity.User> activeUserWrapper = new QueryWrapper<>();
            activeUserWrapper.eq("status", 1);
            LocalDateTime activeStartTime = now.minusDays(7);
            activeUserWrapper.ge("last_login_time", activeStartTime);
            Long activeUsers = userMapper.selectCount(activeUserWrapper);
            result.put("activeUsers", activeUsers != null ? activeUsers : 0L);

            // 3. 总浏览量（所有攻略的view_count总和）
            QueryWrapper<TravelPlan> viewWrapper = new QueryWrapper<>();
            viewWrapper.eq("status", 1);
            if (startTime != null) {
                viewWrapper.ge("create_time", startTime);
            }
            viewWrapper.select("SUM(view_count) as totalViews");
            List<Map<String, Object>> viewList = travelPlanMapper.selectMaps(viewWrapper);
            Long totalViews = 0L;
            if (viewList != null && !viewList.isEmpty()) {
                Map<String, Object> firstRow = viewList.get(0);
                Object totalViewsObj = firstRow != null ? firstRow.get("totalViews") : null;
                if (totalViewsObj != null) {
                    totalViews = ((Number) totalViewsObj).longValue();
                }
            }
            result.put("totalViews", totalViews);

            // 4. 总评论数
            QueryWrapper<com.zhly.entity.Comment> commentWrapper = new QueryWrapper<>();
            commentWrapper.eq("content_type", "PLAN");
            if (startTime != null) {
                commentWrapper.ge("created_time", startTime);
            }
            Long totalComments = commentMapper.selectCount(commentWrapper);
            result.put("totalComments", totalComments != null ? totalComments : 0L);

            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取社区统计失败: " + e.getMessage());
        }
    }

    private void appendUsageMetric(Map<String, Object> container, String key, long currentValue, long previousValue) {
        container.put(key, currentValue);
        double trend = calculateBoundedGrowth(currentValue, previousValue);
        container.put(key + "Trend", Math.round(trend * 100.0) / 100.0);
        container.put(key + "Previous", previousValue);
    }

    private long countTravelPlans(LocalDateTime start, LocalDateTime end) {
        QueryWrapper<TravelPlan> wrapper = new QueryWrapper<>();
        if (start != null) {
            wrapper.ge("create_time", start);
        }
        if (end != null) {
            wrapper.lt("create_time", end);
        }
        Long count = travelPlanMapper.selectCount(wrapper);
        return count != null ? count : 0L;
    }

    private long countAiRequests(LocalDateTime start, LocalDateTime end) {
        QueryWrapper<com.zhly.entity.AiGenerateLog> wrapper = new QueryWrapper<>();
        if (start != null) {
            wrapper.ge("create_time", start);
        }
        if (end != null) {
            wrapper.lt("create_time", end);
        }
        Long count = aiGenerateLogMapper.selectCount(wrapper);
        return count != null ? count : 0L;
    }

    private long countBrowseByTypes(List<Integer> types, LocalDateTime start, LocalDateTime end) {
        if (types == null || types.isEmpty()) {
            return 0L;
        }
        QueryWrapper<com.zhly.entity.UserBrowseHistory> wrapper = new QueryWrapper<>();
        wrapper.in("browse_type", types);
        if (start != null) {
            wrapper.ge("create_time", start);
        }
        if (end != null) {
            wrapper.lt("create_time", end);
        }
        Long count = browseHistoryMapper.selectCount(wrapper);
        return count != null ? count : 0L;
    }

    private long countComments(LocalDateTime start, LocalDateTime end) {
        QueryWrapper<com.zhly.entity.Comment> wrapper = new QueryWrapper<>();
        if (start != null) {
            wrapper.ge("created_time", start);
        }
        if (end != null) {
            wrapper.lt("created_time", end);
        }
        Long count = commentMapper.selectCount(wrapper);
        return count != null ? count : 0L;
    }

    private long countLikes(LocalDateTime start, LocalDateTime end) {
        QueryWrapper<com.zhly.entity.UserLike> wrapper = new QueryWrapper<>();
        if (start != null) {
            wrapper.ge("create_time", start);
        }
        if (end != null) {
            wrapper.lt("create_time", end);
        }
        Long count = userLikeMapper.selectCount(wrapper);
        return count != null ? count : 0L;
    }

    private long countCollects(LocalDateTime start, LocalDateTime end) {
        QueryWrapper<com.zhly.entity.UserCollect> wrapper = new QueryWrapper<>();
        if (start != null) {
            wrapper.ge("create_time", start);
        }
        if (end != null) {
            wrapper.lt("create_time", end);
        }
        Long count = userCollectMapper.selectCount(wrapper);
        return count != null ? count : 0L;
    }

    private long countInteractions(LocalDateTime start, LocalDateTime end) {
        return countComments(start, end) + countLikes(start, end) + countCollects(start, end);
    }

    private long countCultureUsage(LocalDateTime start, LocalDateTime end) {
        long cultureViews = countBrowseByTypes(Arrays.asList(3, 4), start, end);
        long bookings = countCultureBookings(start, end);
        long applications = countProjectApplications(start, end);
        return cultureViews + bookings + applications;
    }

    private long countCultureBookings(LocalDateTime start, LocalDateTime end) {
        if (cultureBookingMapper == null) {
            return 0L;
        }
        QueryWrapper<com.zhly.entity.CultureBooking> wrapper = new QueryWrapper<>();
        if (start != null) {
            wrapper.ge("create_time", start);
        }
        if (end != null) {
            wrapper.lt("create_time", end);
        }
        Long count = cultureBookingMapper.selectCount(wrapper);
        return count != null ? count : 0L;
    }

    private long countProjectApplications(LocalDateTime start, LocalDateTime end) {
        QueryWrapper<ProjectApplication> wrapper = new QueryWrapper<>();
        if (start != null) {
            wrapper.ge("create_time", start);
        }
        if (end != null) {
            wrapper.lt("create_time", end);
        }
        Long count = projectApplicationMapper.selectCount(wrapper);
        return count != null ? count : 0L;
    }

    private long countOrders(LocalDateTime start, LocalDateTime end) {
        QueryWrapper<com.zhly.entity.Order> wrapper = new QueryWrapper<>();
        if (start != null) {
            wrapper.ge("create_time", start);
        }
        if (end != null) {
            wrapper.lt("create_time", end);
        }
        Long count = orderMapper.selectCount(wrapper);
        return count != null ? count : 0L;
    }

    private long countPortraitUsers(LocalDateTime start, LocalDateTime end) {
        QueryWrapper<com.zhly.entity.UserBrowseHistory> wrapper = new QueryWrapper<>();
        wrapper.select("COUNT(DISTINCT user_id) as userCount");
        if (start != null) {
            wrapper.ge("create_time", start);
        }
        if (end != null) {
            wrapper.lt("create_time", end);
        }
        List<Map<String, Object>> records = browseHistoryMapper.selectMaps(wrapper);
        if (records == null || records.isEmpty()) {
            return 0L;
        }
        Object value = records.get(0).get("userCount");
        return value != null ? ((Number) value).longValue() : 0L;
    }

    private FunctionUsageWindow resolveFunctionUsageWindow(String range) {
        String normalized = range != null ? range.trim().toLowerCase() : "week";
        long days;
        String label;
        switch (normalized) {
            case "today":
                days = 1;
                label = "今日实时";
                break;
            case "month":
                days = 30;
                label = "近30天";
                break;
            case "quarter":
            case "all":
                days = 90;
                label = "近90天";
                normalized = "quarter";
                break;
            default:
                days = 7;
                label = "近7天";
                normalized = "week";
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentStart = "today".equals(normalized) ? LocalDate.now().atStartOfDay() : now.minusDays(days);
        LocalDateTime previousEnd = currentStart;
        LocalDateTime previousStart = previousEnd.minusDays(days);
        return new FunctionUsageWindow(normalized, label, days, currentStart, now, previousStart, previousEnd);
    }

    private static class FunctionUsageWindow {
        private final String rangeKey;
        private final String rangeLabel;
        private final long durationDays;
        private final LocalDateTime currentStart;
        private final LocalDateTime currentEnd;
        private final LocalDateTime previousStart;
        private final LocalDateTime previousEnd;

        FunctionUsageWindow(String rangeKey, String rangeLabel, long durationDays,
                            LocalDateTime currentStart, LocalDateTime currentEnd,
                            LocalDateTime previousStart, LocalDateTime previousEnd) {
            this.rangeKey = rangeKey;
            this.rangeLabel = rangeLabel;
            this.durationDays = durationDays;
            this.currentStart = currentStart;
            this.currentEnd = currentEnd;
            this.previousStart = previousStart;
            this.previousEnd = previousEnd;
        }

        public String getRangeKey() {
            return rangeKey;
        }

        public String getRangeLabel() {
            return rangeLabel;
        }

        public long getDurationDays() {
            return durationDays;
        }

        public LocalDateTime getCurrentStart() {
            return currentStart;
        }

        public LocalDateTime getCurrentEnd() {
            return currentEnd;
        }

        public LocalDateTime getPreviousStart() {
            return previousStart;
        }

        public LocalDateTime getPreviousEnd() {
            return previousEnd;
        }
    }

    /**
     * 计算对称增长率，结果限定在 -100% 至 100% 之间，避免过大的百分比。
     */
    private double calculateBoundedGrowth(Long todayValue, Long yesterdayValue) {
        long today = todayValue != null ? todayValue : 0L;
        long yesterday = yesterdayValue != null ? yesterdayValue : 0L;
        long denominator = Math.max(Math.abs(today), Math.abs(yesterday));
        if (denominator == 0) {
            return 0.0;
        }
        double rate = (double) (today - yesterday) / denominator * 100;
        if (rate > 100.0) {
            return 100.0;
        }
        if (rate < -100.0) {
            return -100.0;
        }
        return rate;
    }
}

