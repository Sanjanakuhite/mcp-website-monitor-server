package com.example.mcp.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class ToolService {

    private final RestTemplate restTemplate;

    private static final String API_MONITOR_BASE_URL = "http://localhost:8080";

    public ToolService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;

    }

    public Map<String, Object> discoverApis(String websiteUrl) {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                API_MONITOR_BASE_URL + "/api/discovery/discover",
                Map.of("websiteUrl", websiteUrl),
                Map.class
        );

        Map body = response.getBody();

        return Map.of(
                "website_url", body.getOrDefault("websiteUrl", ""),
                "raw_apis", body.getOrDefault("rawApis", List.of()),
                "discovery_status", body.getOrDefault("discoveryStatus", "NOT_DISCOVERED")
        );
    }

    public Map<String, Object> monitorBatch(List<String> apis) {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                API_MONITOR_BASE_URL + "/api/monitor/check-batch",
                Map.of("apiBatch", apis),
                Map.class
        );

        return response.getBody();

    }

}