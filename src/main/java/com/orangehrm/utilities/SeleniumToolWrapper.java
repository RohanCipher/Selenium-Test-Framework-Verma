package com.orangehrm.utilities;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import java.util.Map;

public class SeleniumToolWrapper {

    private final McpAgentClient mcpClient;

    public SeleniumToolWrapper(McpAgentClient mcpClient) {
        this.mcpClient = mcpClient;
    }

    @Tool("Starts a new browser session. You MUST call this before navigating.")
    public String startBrowser(
            @P("The browser to launch: must be exactly 'chrome', 'firefox', 'edge', or 'safari'") String browser) {

        return mcpClient.executeTool("start_browser", Map.of("browser", (Object) browser));
    }

    @Tool("Navigates the active browser to the specified URL")
    public String navigateToUrl(
            @P("The full HTTP URL to navigate to") String url) {

        // The server expects the tool name to be "navigate" and the argument "url"
        return mcpClient.executeTool("navigate", Map.of("url", (Object) url));
    }

    @Tool("Interacts with a web element on the screen (click, hover, etc.)")
    public String interact(
            @P("The action to perform: must be exactly 'click', 'doubleclick', 'rightclick', or 'hover'") String action,
            @P("The locator strategy: must be exactly 'id', 'css', 'xpath', 'name', 'tag', or 'class'") String by,
            @P("The locator value to find the element") String value) {

        return mcpClient.executeTool("interact", Map.of(
                "action", (Object) action,
                "by", (Object) by,
                "value", (Object) value));
    }
}