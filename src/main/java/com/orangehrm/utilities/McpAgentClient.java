package com.orangehrm.utilities;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.nio.file.Files;
import java.nio.file.Path;

import java.time.Duration;
import java.util.Map;
import java.util.List;

public class McpAgentClient {

    private McpSyncClient mcpClient;

    public static void main(String[] args) {
        McpAgentClient client = new McpAgentClient();
        client.startServer();
        client.printAvailableTools();
    }

    public void startServer() {
        ServerParameters params = null;

        try {
            Path cfg = Path.of("mcp.json");
            if (Files.exists(cfg)) {
                // Read the JSON file
                JsonMapper localMapper = JsonMapper.builder().build();
                @SuppressWarnings("unchecked")
                Map<String, Object> cfgMap = localMapper.readValue(Files.readString(cfg), Map.class);

                // Extract the "command" array
                @SuppressWarnings("unchecked")
                List<String> commandArray = (List<String>) cfgMap.get("command");

                if (commandArray != null && !commandArray.isEmpty()) {
                    // The first string is the executable ("npx.cmd")
                    ServerParameters.Builder paramsBuilder = ServerParameters.builder(commandArray.get(0));

                    // The rest are the arguments
                    if (commandArray.size() > 1) {
                        paramsBuilder.args(commandArray.subList(1, commandArray.size()));
                    }

                    params = paramsBuilder.build();
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse mcp.json: " + e.getMessage());
        }

        // Fallback just in case the file isn't found
        if (params == null) {
            System.out.println("mcp.json not found or invalid, using hardcoded fallback...");
            params = ServerParameters.builder("npx.cmd")
                    .args(List.of("-y", "@angiejones/mcp-selenium"))
                    .build();
        }

        // Initialize Jackson's specialized JsonMapper using its builder
        JsonMapper jsonMapperEngine = JsonMapper.builder().build();
        McpJsonMapper mcpJsonMapper = new JacksonMcpJsonMapper(jsonMapperEngine);

        StdioClientTransport transport = new StdioClientTransport(params, mcpJsonMapper);

        this.mcpClient = McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(60))
                .build();

        this.mcpClient.initialize();
    }

    public void printAvailableTools() {
        ListToolsResult toolsResult = this.mcpClient.listTools();

        System.out.println("--- Available Selenium Tools ---");
        for (Tool tool : toolsResult.tools()) {
            System.out.println("Tool Name: " + tool.name());
            System.out.println("Description: " + tool.description());
            System.out.println("Required Arguments: " + tool.inputSchema());
            System.out.println("--------------------------------");
        }
    }

    public String executeTool(String toolName, Map<String, Object> arguments) {
        System.out.println("🤖 AI ATTEMPTED TO USE TOOL: " + toolName + " with args: " + arguments);
        try {
            CallToolRequest request = new CallToolRequest(toolName, arguments);
            CallToolResult result = this.mcpClient.callTool(request);

            // ADD THIS LINE: Print exactly what the Node server tells Java
            System.out.println("🛠️ TOOL RESULT: " + result.content());

            // ✅ SAFELY check if it's an error. If it's null, it evaluates to false instead
            // of crashing.
            if (Boolean.TRUE.equals(result.isError())) {
                return "Execution Error: " + result.content();
            }

            return result.content().toString();

        } catch (Exception e) {
            System.out.println("💥 FRAMEWORK CRASH: " + e.getMessage());
            return "Framework Error: Failed to execute " + toolName + " - " + e.getMessage();
        }
    }
}