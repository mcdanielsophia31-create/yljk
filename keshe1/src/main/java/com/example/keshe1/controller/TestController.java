package com.example.keshe1.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {

    @Value("${doubao.api.ak:#{null}}")
    private String accessKey;

    @Value("${doubao.api.sk:#{null}}")
    private String secretKey;

    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> testConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("ak", accessKey != null ? "配置已设置" : "未配置");
        config.put("sk", secretKey != null ? "配置已设置" : "未配置");
        config.put("ak_value", accessKey != null ? accessKey.substring(0, Math.min(5, accessKey.length())) + "..." : "N/A");
        config.put("status", "API配置检查完成");
        
        return ResponseEntity.ok(config);
    }
}