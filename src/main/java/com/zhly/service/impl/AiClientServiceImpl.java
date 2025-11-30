package com.zhly.service.impl;

import com.zhly.service.AiClientService;
import com.zhly.config.AiConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * AI客户端服务实现类
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Service
public class AiClientServiceImpl implements AiClientService {
    
    @Autowired
    private AiConfig aiConfig;
    
    @Autowired
    @Qualifier("aiRestTemplate")
    private RestTemplate aiRestTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public String generateContent(String prompt, String modelName, Integer maxTokens, Double temperature) {
        // 使用真实API调用
        try {
            // 检查是否是文心一言
            String actualModelName = aiConfig.getModelName();
            System.out.println("当前配置的模型: " + actualModelName);
            System.out.println("API Key: " + (aiConfig.getApiKey() != null ? aiConfig.getApiKey().substring(0, Math.min(20, aiConfig.getApiKey().length())) + "..." : "null"));
            
            if (actualModelName != null && (actualModelName.contains("文心") || actualModelName.contains("ernie"))) {
                System.out.println("使用文心一言API");
                return generateWenxinContent(prompt, actualModelName, maxTokens, temperature);
            }
            // 使用OpenAI格式
            System.out.println("使用OpenAI格式API");
            return generateContentWithSystemMessage(null, prompt, modelName, maxTokens, temperature);
        } catch (Exception e) {
            // 如果真实API调用失败，使用模拟数据
            System.err.println("真实API调用失败，使用模拟数据: " + e.getMessage());
            e.printStackTrace();
            return generateMockContent(prompt);
        }
    }
    
    /**
     * 调用文心一言API生成内容
     */
    private String generateWenxinContent(String prompt, String modelName, Integer maxTokens, Double temperature) {
        try {
            // 解析密钥：bce-v3/{API_KEY}/{SECRET_KEY}
            String apiKey = aiConfig.getApiKey();
            System.out.println("开始调用文心一言API");
            System.out.println("完整密钥长度: " + (apiKey != null ? apiKey.length() : 0));
            
            if (apiKey == null || apiKey.isEmpty()) {
                throw new RuntimeException("API密钥未配置");
            }
            
            String[] keyParts = apiKey.split("/");
            System.out.println("密钥分段数量: " + keyParts.length);
            
            if (keyParts.length < 3) {
                throw new RuntimeException("文心一言密钥格式错误，应为: bce-v3/{API_KEY}/{SECRET_KEY}，当前格式: " + apiKey);
            }
            
            String apiKeyPart = keyParts[1];
            String secretKeyPart = keyParts[2];
            System.out.println("API Key: " + apiKeyPart.substring(0, Math.min(15, apiKeyPart.length())) + "...");
            System.out.println("Secret Key长度: " + secretKeyPart.length());
            
            // 获取access_token
            System.out.println("正在获取access_token...");
            String accessToken = getWenxinAccessToken(apiKeyPart, secretKeyPart);
            System.out.println("access_token获取成功");
            
            // 构建请求
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "ernie-bot-turbo");
            requestBody.put("messages", List.of(Map.of("role", "user", "content", prompt)));
            requestBody.put("max_output_tokens", maxTokens);
            requestBody.put("temperature", temperature);
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            System.out.println("正在调用文心一言API: " + aiConfig.getApiUrl());
            
            // 发送请求
            ResponseEntity<String> response = aiRestTemplate.postForEntity(
                aiConfig.getApiUrl(),
                request,
                String.class
            );
            
            System.out.println("API响应状态: " + response.getStatusCode());
            System.out.println("API响应内容: " + response.getBody());
            
            if (response.getStatusCode() == HttpStatus.OK) {
                // 解析响应
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                JsonNode result = jsonNode.get("result");
                if (result != null) {
                    System.out.println("成功获取AI生成内容");
                    return result.asText();
                }
            }
            
            System.out.println("文心一言API未返回有效内容");
            return "文心一言API未返回有效内容";
            
        } catch (Exception e) {
            System.err.println("文心一言API调用失败详情: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("文心一言API调用失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取文心一言access_token
     */
    private String getWenxinAccessToken(String apiKey, String secretKey) {
        try {
            String tokenUrl = "https://aip.baidubce.com/oauth/2.0/token";
            
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            // Use MultiValueMap for automatic URL encoding
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials");
            body.add("client_id", apiKey);
            body.add("client_secret", secretKey);
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            
            System.out.println("Token请求URL: " + tokenUrl);
            System.out.println("API Key: " + apiKey.substring(0, Math.min(15, apiKey.length())) + "...");
            System.out.println("Secret Key长度: " + secretKey.length());
            System.out.println("完整API Key: " + apiKey);
            System.out.println("完整Secret Key前10位: " + secretKey.substring(0, Math.min(10, secretKey.length())));
            
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);
            
            System.out.println("Token响应状态: " + response.getStatusCode());
            System.out.println("Token响应内容: " + response.getBody());
            
            // 如果失败，打印详细错误信息
            if (response.getStatusCode() != HttpStatus.OK) {
                System.err.println("获取token失败！");
                System.err.println("状态码: " + response.getStatusCode());
                System.err.println("响应头: " + response.getHeaders());
                System.err.println("响应体: " + response.getBody());
            }
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                JsonNode accessToken = jsonNode.get("access_token");
                if (accessToken != null) {
                    System.out.println("access_token获取成功");
                    return accessToken.asText();
                }
                // 检查错误
                JsonNode error = jsonNode.get("error");
                JsonNode errorDescription = jsonNode.get("error_description");
                if (error != null) {
                    throw new RuntimeException("获取access_token失败: " + error.asText() + " - " + (errorDescription != null ? errorDescription.asText() : ""));
                }
            }
            
            throw new RuntimeException("获取access_token失败: HTTP状态码 " + response.getStatusCode());
            
        } catch (Exception e) {
            System.err.println("获取文心一言access_token失败详情: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("获取文心一言access_token失败: " + e.getMessage());
        }
    }
    
    /**
     * 模拟AI生成内容（用于测试，不需要真实API）
     */
    private String generateMockContent(String prompt) {
        // 模拟响应时间
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 根据提示词生成模拟回复
        if (prompt.contains("重庆") || prompt.contains("旅游") || prompt.contains("攻略")) {
            return "📋 【重庆】旅游攻略\n\n" +
                   "🎯 行程概览\n" +
                   "• 重庆是一座山城，拥有独特的立体交通和丰富的美食文化。\n\n" +
                   "📅 推荐景点\n" +
                   "• 解放碑 - 重庆的标志性景点\n" +
                   "• 洪崖洞 - 欣赏吊脚楼建筑\n" +
                   "• 磁器口古镇 - 体验老重庆风情\n\n" +
                   "🍜 美食推荐\n" +
                   "• 重庆火锅 - 麻辣鲜香\n" +
                   "• 小面 - 重庆特色面食\n\n" +
                   "💡 实用贴士\n" +
                   "• 重庆气候湿热，建议多喝水\n" +
                   "• 轻轨是最方便的交通方式";
        } else if (prompt.contains("介绍") || prompt.contains("城市")) {
            return "这是一座充满活力的城市，拥有丰富的历史文化和现代发展。这里的人们热情好客，美食多样，风景优美。无论是历史文化还是自然风光，都值得一游。";
        } else {
            return "这是一个很好的问题。根据您的需求，我建议您可以考虑以下几点：\n\n" +
                   "1. 首先明确目标\n" +
                   "2. 制定详细的计划\n" +
                   "3. 分步骤执行\n" +
                   "4. 及时调整优化\n\n" +
                   "希望这些建议对您有帮助！";
        }
    }
    
    /**
     * 带系统提示词的内容生成
     */
    public String generateContentWithSystemMessage(String systemPrompt, String userPrompt, String modelName, Integer maxTokens, Double temperature) {
        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("temperature", temperature);
            
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 添加系统提示词（如果有）
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                Map<String, String> systemMessage = new HashMap<>();
                systemMessage.put("role", "system");
                systemMessage.put("content", systemPrompt);
                messages.add(systemMessage);
            }
            
            // 添加用户消息
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", userPrompt);
            messages.add(message);
            requestBody.put("messages", messages);
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + aiConfig.getApiKey());
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            // 发送请求
            ResponseEntity<String> response = aiRestTemplate.postForEntity(
                aiConfig.getApiUrl(), 
                request, 
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                // 解析响应
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                JsonNode choices = jsonNode.get("choices");
                if (choices != null && choices.isArray() && choices.size() > 0) {
                    JsonNode firstChoice = choices.get(0);
                    JsonNode messageNode = firstChoice.get("message");
                    if (messageNode != null) {
                        return messageNode.get("content").asText();
                    }
                }
            }
            
            return "AI服务暂时不可用，请稍后重试";
            
        } catch (Exception e) {
            // 记录日志
            System.err.println("AI API调用失败: " + e.getMessage());
            return "AI服务调用失败: " + e.getMessage();
        }
    }
    
    /**
     * 生成旅游攻略（使用标准模板）
     */
    public String generateTravelPlanWithTemplate(Map<String, Object> params) {
        String destination = (String) params.get("destination");
        Integer days = (Integer) params.get("days");
        String budget = (String) params.get("budget");
        String interests = (String) params.get("interests");
        String travelStyle = (String) params.get("travelStyle");
        
        // 系统提示词 - 设定AI角色和输出格式
        String systemPrompt = "你是一个专业的旅游攻略生成助手。请严格按照以下格式输出旅游攻略：\n\n" +
            "📋 【目的地】X天旅游攻略\n\n" +
            "🎯 行程概览\n" +
            "• 目的地：[目的地]\n" +
            "• 天数：[天数]\n" +
            "• 预算：[预算]\n" +
            "• 风格：[旅游风格]\n\n" +
            "📅 每日行程安排\n\n" +
            "🌟 第一天：[日期/主题]\n" +
            "上午：\n" +
            "  • [景点/活动名称] - [简单描述]\n" +
            "  • 时间安排：[时间段]\n" +
            "  • 交通方式：[交通信息]\n\n" +
            "下午：\n" +
            "  • [景点/活动名称] - [简单描述]\n" +
            "  • 时间安排：[时间段]\n" +
            "  • 交通方式：[交通信息]\n\n" +
            "晚上：\n" +
            "  • [晚餐/活动] - [推荐地点]\n\n" +
            "🏨 住宿建议：\n" +
            "• [推荐酒店/民宿名称] - [价格区间] - [推荐理由]\n\n" +
            "（为每一天重复上述格式）\n\n" +
            "🍜 美食推荐\n" +
            "• [美食名称] - [特点] - [推荐餐厅/地点] - [价格区间]\n\n" +
            "🚗 交通指南\n" +
            "• 到达：[交通方式] - [详细信息]\n" +
            "• 市内交通：[主要交通方式] - [实用信息]\n\n" +
            "💡 实用贴士\n" +
            "• [重要提示或注意事项]\n\n" +
            "💵 费用预估\n" +
            "• 住宿：[预估费用]\n" +
            "• 餐饮：[预估费用]\n" +
            "• 交通：[预估费用]\n" +
            "• 门票：[预估费用]\n" +
            "• 总计：[总费用]\n\n" +
            "⚠️ 注意事项\n" +
            "• [注意事项]\n\n" +
            "请注意：必须严格按照上述格式输出，确保结构清晰、信息完整！";
        
        // 用户提示词 - 具体的需求
        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("请为").append(destination).append("生成").append(days).append("天的旅游攻略。\n\n");
        userPrompt.append("具体要求：\n");
        userPrompt.append("• 预算：").append(budget).append("\n");
        userPrompt.append("• 兴趣爱好：").append(interests).append("\n");
        userPrompt.append("• 旅游风格：").append(travelStyle).append("\n\n");
        userPrompt.append("请确保：\n");
        userPrompt.append("1. 行程安排合理，不走回头路\n");
        userPrompt.append("2. 景点选择符合用户的兴趣爱好\n");
        userPrompt.append("3. 预算控制在指定范围内\n");
        userPrompt.append("4. 内容实用详细，包含具体的地址、开放时间等信息\n");
        userPrompt.append("5. 严格按照上面的格式输出");
        
        return generateContentWithSystemMessage(systemPrompt, userPrompt.toString(), aiConfig.getModelName(), aiConfig.getMaxTokens(), aiConfig.getTemperature());
    }
    
    @Override
    public String generateTravelPlan(Map<String, Object> params) {
        // 使用带模板的方法生成旅游攻略
        return generateTravelPlanWithTemplate(params);
    }
    
    @Override
    public String chat(String question) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的旅游顾问，请回答以下问题：\n");
        prompt.append(question).append("\n");
        prompt.append("请用中文回答，内容要专业、准确、实用。");
        
        return generateContent(prompt.toString(), aiConfig.getModelName(), aiConfig.getMaxTokens(), aiConfig.getTemperature());
    }
    
    @Override
    public String generateAttractionRecommendation(String destination, String interests) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请为").append(destination).append("推荐适合的景点。\n");
        prompt.append("用户兴趣：").append(interests).append("\n");
        prompt.append("请推荐5-8个景点，包括：\n");
        prompt.append("1. 景点名称\n");
        prompt.append("2. 景点特色\n");
        prompt.append("3. 游览时间\n");
        prompt.append("4. 门票价格\n");
        prompt.append("5. 交通方式\n");
        prompt.append("请用中文回答，内容要详细实用。");
        
        return generateContent(prompt.toString(), aiConfig.getModelName(), aiConfig.getMaxTokens(), aiConfig.getTemperature());
    }
    
    @Override
    public String generateFoodRecommendation(String destination, String preferences) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请为").append(destination).append("推荐当地美食。\n");
        prompt.append("用户偏好：").append(preferences).append("\n");
        prompt.append("请推荐5-8个美食，包括：\n");
        prompt.append("1. 美食名称\n");
        prompt.append("2. 美食特色\n");
        prompt.append("3. 推荐餐厅\n");
        prompt.append("4. 价格范围\n");
        prompt.append("5. 品尝建议\n");
        prompt.append("请用中文回答，内容要详细实用。");
        
        return generateContent(prompt.toString(), aiConfig.getModelName(), aiConfig.getMaxTokens(), aiConfig.getTemperature());
    }
}
