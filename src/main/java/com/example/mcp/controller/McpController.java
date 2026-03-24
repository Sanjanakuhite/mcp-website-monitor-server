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

        String toolName = (String) request.get("tool_name");
        Map<String, Object> args = (Map<String, Object>) request.get("arguments");

        if ("discover_website_apis".equals(toolName)) {
            String url = (String) args.get("website_url");
            return ResponseEntity.ok(Map.of(
                    "tool_name", toolName,
                    "result", toolService.discoverApis(url)
            ));
        }

        if ("monitor_api_batch".equals(toolName)) {
            List<String> batch = (List<String>) args.get("api_batch");
            return ResponseEntity.ok(Map.of(
                    "tool_name", toolName,
                    "result", toolService.monitorBatch(batch)
            ));
        }

        return ResponseEntity.badRequest().body(Map.of("error", "Unknown tool"));
    }
}
