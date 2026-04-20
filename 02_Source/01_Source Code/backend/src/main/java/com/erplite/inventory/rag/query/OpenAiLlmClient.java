package com.erplite.inventory.rag.query;

import com.erplite.inventory.rag.RagProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class OpenAiLlmClient implements LlmClient {

    private final RestClient client;
    private final String model;

    public OpenAiLlmClient(@Qualifier("openAiRestClient") RestClient client, RagProperties props) {
        this.client = client;
        this.model = props.openai().chatModel();
    }

    @Override
    public String complete(List<Map<String, String>> messages, double temperature, int maxTokens) {
        return callChat(messages, temperature, maxTokens, null);
    }

    @Override
    public String completeJson(List<Map<String, String>> messages, double temperature, int maxTokens) {
        return callChat(messages, temperature, maxTokens, Map.of("type", "json_object"));
    }

    @Override
    public String modelId() { return model; }

    @SuppressWarnings("unchecked")
    private String callChat(List<Map<String, String>> messages, double temperature, int maxTokens,
                            Map<String, Object> responseFormat) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", temperature);
        body.put("max_tokens", maxTokens);
        if (responseFormat != null) body.put("response_format", responseFormat);

        Map<String, Object> resp = client.post()
                .uri("/chat/completions")
                .body(body)
                .retrieve()
                .body(Map.class);
        if (resp == null) throw new IllegalStateException("Empty LLM response");
        List<Map<String, Object>> choices = (List<Map<String, Object>>) resp.get("choices");
        if (choices == null || choices.isEmpty()) throw new IllegalStateException("No choices in response");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return String.valueOf(message.get("content"));
    }
}
