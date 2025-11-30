package com.zhly.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.util.Timeout;

/**
 * 第三方API配置类
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Configuration
public class ThirdPartyApiConfig {
    
    @Value("${third-party.weather.api.url:https://api.openweathermap.org/data/2.5/weather}")
    private String weatherApiUrl;
    
    @Value("${third-party.weather.api.key:your-weather-api-key}")
    private String weatherApiKey;
    
    @Value("${third-party.amap.api.url:https://restapi.amap.com}")
    private String amapApiUrl;
    
    @Value("${third-party.amap.api.key:your-amap-api-key}")
    private String amapApiKey;
    
    @Value("${third-party.12306.api.url:https://kyfw.12306.cn}")
    private String trainApiUrl;
    
    @Value("${third-party.timeout:10000}")
    private Integer timeout;
    
    @Bean
    public RestTemplate thirdPartyRestTemplate() {
        // 创建连接池
        HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(50)
                .setMaxConnPerRoute(10)
                .build();
        
        // 配置请求超时
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(timeout))
                .setResponseTimeout(Timeout.ofMilliseconds(timeout))
                .build();
        
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
        
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(java.time.Duration.ofMillis(timeout));
        
        return new RestTemplate(factory);
    }
    
    // Getter方法
    public String getWeatherApiUrl() {
        return weatherApiUrl;
    }
    
    public String getWeatherApiKey() {
        return weatherApiKey;
    }
    
    public String getAmapApiUrl() {
        return amapApiUrl;
    }
    
    public String getAmapApiKey() {
        return amapApiKey;
    }
    
    public String getTrainApiUrl() {
        return trainApiUrl;
    }
}
