package com.orangehrm.utilities;

import org.apache.xmlbeans.impl.xb.xsdschema.Attribute.Use;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface AutomationAgent {

    @SystemMessage({
            "You are an expert QA automation engineer.",
            "Step 1: You MUST always call 'startBrowser' first to initialize the session.",
            "Step 2: Use 'navigateToUrl' to open the target webpage.",
            "Step 3: Interact with the page.",
            "Step 4: If you need to click or interact with an element, use the 'interact' tool with the most specific locator possible (id > css > xpath).",
            "Note 1: Use xpath instead of css wherever possible, use double quotes for xpath attribute values.",
            "Note 2: Use double quotes for xpath attribute values.",
            "CRITICAL: If you receive an 'element not interactable' error, it means you targeted a hidden DOM element. "
                    +
                    "You must immediately try a different, more specific locator (like a precise CSS selector or a parent-child XPath) "
                    +
                    "to find the visible version of the element on the screen."
    })
    String executeTest(@UserMessage String prompt);
}