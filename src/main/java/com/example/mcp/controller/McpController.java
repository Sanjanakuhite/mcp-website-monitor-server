package com.example.mcp.controller;

import com.example.mcp.service.ToolService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mcp")
@CrossOrigin(origins = "*")
public class McpController {

    private final ToolService toolService;
    private final ObjectMapper objectMapper;

    public McpController(ToolService toolService, ObjectMapper objectMapper) {
        this.toolService = toolService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<?> getTools() {
        return ResponseEntity.ok(buildToolsResponse());
    }

    @PostMapping
    public ResponseEntity<?> handle(@RequestBody Map<String, Object> request) {
        try {
            String type = request.get("type") != null ? request.get("type").toString() : null;
            String method = request.get("method") != null ? request.get("method").toString() : null;

            // MCP / Builder tool listing
            if ("list_tools".equals(type) || "tools/list".equals(method)) {
                return ResponseEntity.ok(Map.of(
                        "content", List.of(
                                Map.of(
                                        "type", "text",
                                        "text", objectMapper.writeValueAsString(buildToolsResponse())
                                )
                        )
                ));
            }

            // MCP / Builder tool execution
            if ("tools/call".equals(method)) {
                Object paramsObj = request.get("params");
                if (!(paramsObj instanceof Map<?, ?> paramsRaw)) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", "Missing or invalid params"
                    ));
                }

                String toolName = paramsRaw.get("name") != null ? paramsRaw.get("name").toString() : null;

                Object argsObj = paramsRaw.get("arguments");
                if (!(argsObj instanceof Map<?, ?> rawArgs)) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", "Missing or invalid arguments"
                    ));
                }

                Map<String, Object> args = (Map<String, Object>) rawArgs;
                return executeTool(toolName, args);
            }

            // Manual Postman fallback
            String toolName = request.get("tool_name") != null ? request.get("tool_name").toString() : null;

            Object argsObj = request.get("arguments");
            if (!(argsObj instanceof Map<?, ?> rawArgs)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Missing or invalid arguments"
                ));
            }

            Map<String, Object> args = (Map<String, Object>) rawArgs;
            return executeTool(toolName, args);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", e.getMessage() != null ? e.getMessage() : "Internal error"
            ));
        }
    }

    private ResponseEntity<?> executeTool(String toolName, Map<String, Object> args) {
        if ("discover_website_apis".equals(toolName)) {
            String url = args.get("website_url") != null
                    ? args.get("website_url").toString()
                    : "";

            Object result = toolService.discoverApis(url);

            return ResponseEntity.ok(Map.of(
                    "content", List.of(
                            Map.of(
                                    "type", "text",
                                    "text", result != null ? result.toString() : "No result"
                            )
                    )
            ));
        }

        if ("monitor_api_batch".equals(toolName)) {
            Object raw = args.get("api_batch");

            if (!(raw instanceof List<?> rawList)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "api_batch must be a list"
                ));
            }

            List<String> batch = rawList.stream()
                    .map(String::valueOf)
                    .toList();

            Object result = toolService.monitorBatch(batch);

            return ResponseEntity.ok(Map.of(
                    "content", List.of(
                            Map.of(
                                    "type", "text",
                                    "text", result != null ? result.toString() : "No result"
                            )
                    )
            ));
        }

        return ResponseEntity.badRequest().body(Map.of(
                "error", "Unknown tool: " + toolName
        ));
    }

    private Map<String, Object> buildToolsResponse() {
        return Map.of(
                "tools", List.of(
                        Map.of(
                                "name", "discover_website_apis",
                                "description", "Discover APIs from a website",
                                "input_schema", Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "website_url", Map.of(
                                                        "type", "string"
                                                )
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
                                                        "items", Map.of(
                                                                "type", "string"
                                                        )
                                                )
                                        ),
                                        "required", List.of("api_batch")
                                )
                        )
                )
        );
    }
}