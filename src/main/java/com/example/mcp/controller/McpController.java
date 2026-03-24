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

    @GetMapping
    public ResponseEntity<?> getTools() {
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

    @PostMapping
    public ResponseEntity<?> handle(@RequestBody Map<String, Object> request) {
        try {
            String type = (String) request.get("type");
            String method = (String) request.get("method");

            // MCP / Builder tool listing support
            if ("list_tools".equals(type) || "tools/list".equals(method)) {
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

            // MCP / Builder tool call support
            if ("tools/call".equals(method)) {
                Map<String, Object> params = (Map<String, Object>) request.get("params");
                if (params == null) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", "Missing params"
                    ));
                }

                String toolName = (String) params.get("name");
                Map<String, Object> args = (Map<String, Object>) params.get("arguments");

                if ("discover_website_apis".equals(toolName)) {
                    String url = args.get("website_url") != null
                            ? args.get("website_url").toString()
                            : "";

                    return ResponseEntity.ok(Map.of(
                            "content", List.of(
                                    Map.of(
                                            "type", "text",
                                            "text", toolService.discoverApis(url).toString()
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
                            .map(Object::toString)
                            .toList();

                    return ResponseEntity.ok(Map.of(
                            "content", List.of(
                                    Map.of(
                                            "type", "text",
                                            "text", toolService.monitorBatch(batch).toString()
                                    )
                            )
                    ));
                }

                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Unknown tool: " + toolName
                ));
            }

            // fallback for your manual Postman testing
            String toolName = (String) request.get("tool_name");
            Map<String, Object> args = (Map<String, Object>) request.get("arguments");

            if ("discover_website_apis".equals(toolName)) {
                String url = args.get("website_url") != null
                        ? args.get("website_url").toString()
                        : "";

                return ResponseEntity.ok(Map.of(
                        "content", List.of(
                                Map.of(
                                        "type", "text",
                                        "text", toolService.discoverApis(url).toString()
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
                        .map(Object::toString)
                        .toList();

                return ResponseEntity.ok(Map.of(
                        "content", List.of(
                                Map.of(
                                        "type", "text",
                                        "text", toolService.monitorBatch(batch).toString()
                                )
                        )
                ));
            }

            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Unknown request"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", e.getMessage() != null ? e.getMessage() : "Internal error"
            ));
        }
    }
}