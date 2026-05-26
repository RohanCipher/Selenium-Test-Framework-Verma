package com.orangehrm.test;

import com.orangehrm.utilities.AutomationAgent;
import com.orangehrm.utilities.McpAgentClient;
import com.orangehrm.utilities.SeleniumToolWrapper;

import dev.langchain4j.mcp.McpToolProvider;
//import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.HallucinatedToolNameStrategy;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.*;

public class TestRunnerAI {
  public static void main(String[] args) {
    // 1. Initialize your custom client
    McpAgentClient mcpClient = new McpAgentClient();
    mcpClient.startServer();

    // 1. Build the dynamic MCP provider
    // McpToolProvider dynamicTools = McpToolProvider.builder()
    // .mcpClients(List.of(mcpClient)) // Pass your initialized client here
    // .build();

    // 2. Configure the openai LLM
    // OpenAiChatModel chatModel = OpenAiChatModel.builder()
    // .apiKey(System.getenv("OPENAI_API_KEY"))
    // .modelName("gpt-4o")
    // .build();
    String ollamaApiKey;
    try {
      Dotenv dotenv = Dotenv.load();
      ollamaApiKey = dotenv.get("OLLAMA_API_KEY");
    } catch (Exception e) {
      // Fall back to system env if .env file not found
      ollamaApiKey = System.getenv("OLLAMA_API_KEY");
    }

    OllamaChatModel chatModel = OllamaChatModel.builder()
        .baseUrl("http://localhost:11434") // Point to Ollama Cloud instead of localhost
        .modelName("llama3.2") // Replace with your preferred cloud model
        // .customHeaders(Map.of("Authorization", "Bearer " + ollamaApiKey))
        .temperature(0.0) // Keep at 0.0 for strict, deterministic tool execution
        .build();

    // 3. Build the agent and inject the wrapped tools
    AutomationAgent agent = AiServices.builder(AutomationAgent.class)
        .chatModel(chatModel)
        .tools(new SeleniumToolWrapper(mcpClient))
        .hallucinatedToolNameStrategy(HallucinatedToolNameStrategy.THROW_EXCEPTION) // schema
        // .toolProvider(dynamicTools)
        .build();

    // 4. Execute a goal!
    String result = agent.executeTest("Go to https://google.com and click the 'I'm Feeling Lucky' button.");
    System.out.println("Agent Result: " + result);
  }
}
