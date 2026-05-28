package com.example.keshe1.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * AI健康助手控制器 - API Key版（对接豆包AI，无本地话题限制）
 */
@RestController
@RequestMapping("/api/doubao")
public class AIController {

    // 从配置文件读取API Key（替代原有AK/SK）
    @Value("${doubao.api.key}")
    private String apiKey;

    @Value("${doubao.api.endpoint}")
    private String apiEndpoint;

    @Value("${doubao.model}")
    private String modelName;

    private final RestTemplate restTemplate = new RestTemplate();

    // 核心聊天接口 - 直接调用豆包AI（API Key鉴权）
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> request) {
        try {
            // 1. 构建请求头（API Key鉴权）
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // 关键：API Key 鉴权格式（火山方舟API Key标准格式）
            headers.set("Authorization", "Bearer " + apiKey);

            // 2. 构建请求体（透传前端所有参数到豆包AI）
            Map<String, Object> apiRequestBody = new HashMap<>();
            apiRequestBody.put("model", modelName);
            apiRequestBody.put("messages", request.get("messages")); // 透传对话上下文
            apiRequestBody.put("temperature", request.getOrDefault("temperature", 0.7));
            apiRequestBody.put("max_tokens", request.getOrDefault("max_tokens", 1000));

            // 3. 调用豆包AI接口
            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(apiRequestBody, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiEndpoint,
                    HttpMethod.POST,
                    httpEntity,
                    Map.class
            );

            // 4. 直接返回豆包AI的原始响应（兼容前端格式）
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            // 异常兜底：返回标准化错误响应（前端可正常解析）
            Map<String, Object> errorResult = new HashMap<>();
            List<Map<String, Object>> errorChoices = new ArrayList<>();
            Map<String, Object> errorChoice = new HashMap<>();
            Map<String, Object> errorMessage = new HashMap<>();
            errorMessage.put("role", "assistant");
            errorMessage.put("content", "抱歉，AI服务暂时不可用：" + e.getMessage());
            errorChoice.put("message", errorMessage);
            errorChoices.add(errorChoice);
            errorResult.put("choices", errorChoices);
            errorResult.put("model", modelName);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }
}