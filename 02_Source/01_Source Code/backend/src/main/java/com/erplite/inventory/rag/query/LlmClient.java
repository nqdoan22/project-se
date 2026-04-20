package com.erplite.inventory.rag.query;

import java.util.List;
import java.util.Map;

public interface LlmClient {

    /** Chat completion. `messages` = list of {role, content}. */
    String complete(List<Map<String, String>> messages, double temperature, int maxTokens);

    /** Chat completion with JSON response format constraint. */
    String completeJson(List<Map<String, String>> messages, double temperature, int maxTokens);

    String modelId();
}
