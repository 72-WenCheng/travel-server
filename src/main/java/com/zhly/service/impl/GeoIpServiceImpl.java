package com.zhly.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhly.service.GeoIpService;
import com.zhly.service.dto.GeoIpInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class GeoIpServiceImpl implements GeoIpService {

    private static final String LOOKUP_URL = "https://ipapi.co/%s/json/";
    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, GeoIpInfo> cache = new ConcurrentHashMap<>();

    @Override
    public GeoIpInfo lookup(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return null;
        }
        if (isPrivateIp(ipAddress)) {
            return new GeoIpInfo("CN", "中国");
        }
        return cache.computeIfAbsent(ipAddress, this::fetchFromRemote);
    }

    private GeoIpInfo fetchFromRemote(String ipAddress) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(LOOKUP_URL, ipAddress)))
                    .timeout(TIMEOUT)
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode node = objectMapper.readTree(response.body());
                String countryCode = node.path("country_code").asText(null);
                if (countryCode != null && !countryCode.isBlank()) {
                    countryCode = countryCode.toUpperCase(Locale.ROOT);
                    String countryName = toChineseCountryName(countryCode);
                    return new GeoIpInfo(countryCode, countryName);
                }
            } else {
                log.warn("GeoIP lookup failed, status={}, ip={}", response.statusCode(), ipAddress);
            }
        } catch (Exception e) {
            log.warn("GeoIP lookup exception for ip {}: {}", ipAddress, e.getMessage());
        }
        return null;
    }

    private String toChineseCountryName(String countryCode) {
        try {
            Locale locale = new Locale("", countryCode);
            String zhName = locale.getDisplayCountry(Locale.SIMPLIFIED_CHINESE);
            if (zhName == null || zhName.isBlank()) {
                String enName = locale.getDisplayCountry(Locale.ENGLISH);
                return (enName == null || enName.isBlank()) ? countryCode : enName;
            }
            return zhName;
        } catch (Exception e) {
            return countryCode;
        }
    }

    private boolean isPrivateIp(String ip) {
        return ip.startsWith("10.")
                || ip.startsWith("192.168.")
                || ip.startsWith("172.16.")
                || ip.startsWith("172.17.")
                || ip.startsWith("172.18.")
                || ip.startsWith("172.19.")
                || ip.startsWith("172.20.")
                || ip.startsWith("172.21.")
                || ip.startsWith("172.22.")
                || ip.startsWith("172.23.")
                || ip.startsWith("172.24.")
                || ip.startsWith("172.25.")
                || ip.startsWith("172.26.")
                || ip.startsWith("172.27.")
                || ip.startsWith("172.28.")
                || ip.startsWith("172.29.")
                || ip.startsWith("172.30.")
                || ip.startsWith("172.31.")
                || "127.0.0.1".equals(ip)
                || "0:0:0:0:0:0:0:1".equals(ip);
    }
}

