package com.example.mcp.controller;

import com.example.mcp.service.ToolService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mcp")
public class McpController {

    private final ToolService toolService;

    public McpController(ToolService toolService) {
        this.toolService = toolService;
    }

    @PostMapping
    public ResponseEntity<?> handle(@RequestBody Map<String, Object> request) {
        try {
            String type = (String) request.get("type");

            // =========================
            // 1. TOOL DISCOVERY (VERY IMPORTANT)
            // =========================
            if ("list_tools".equals(type)) {
                return ResponseEntity.ok(Map.of(
                        "tools", List.of(
                                Map.of(
                                        "name", "discover_website_apis",
                                        "description", "Discover APIs from a website",
                                        "input_schema", Map.of(
                                                "type", "object",
                                                "properties", Map.of(
                                                        "website_url", Map.of("type", "string")
                                                ),
                                                "required", List.of("website_url")
                                        )
                                ),
                                Map.of(
                                        "name", "monitor_api_batch",
                                        "description", "Monitor API batch health",
                                        "input_schema", Map.of(
                                                "type", "object",
                                                "properties", Map.of(
                                                        "api_batch", Map.of(
                                                                "type", "array",
                                                                "items", Map.of("type", "string")
                                                        )
                                                ),
                                                "required", List.of("api_batch")
                                        )
                                )
                        )
                ));
            }

            // =========================
            // 2. TOOL EXECUTION
            // =========================
            String toolName = (String) request.get("tool_name");
            Map<String, Object> args = (Map<String, Object>) request.get("arguments");

            if ("discover_website_apis".equals(toolName)) {
                String url = args.get("website_url").toString();

                return ResponseEntity.ok(Map.of(
                        "result", toolService.discoverApis(url)
                ));
            }

            if ("monitor_api_batch".equals(toolName)) {
                List<?> rawList = (List<?>) args.get("api_batch");

                List<String> batch = rawList.stream()
                        .map(Object::toString)
                        .toList();

                return ResponseEntity.ok(Map.of(
                        "result", toolService.monitorBatch(batch)
                ));
            }

            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Unknown tool"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }
}