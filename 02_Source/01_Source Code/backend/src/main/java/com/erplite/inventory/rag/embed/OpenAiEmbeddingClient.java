package com.erplite.inventory.rag.embed;

import com.erplite.inventory.rag.RagProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class OpenAiEmbeddingClient implements EmbeddingClient {

    private static final int BATCH_SIZE = 100;

    private final RestClient client;
    private final String model;

    public OpenAiEmbeddingClient(@Qualifier("openAiRestClient") RestClient client, RagProperties props) {
        this.client = client;
        this.model = props.openai().embeddingModel();
    }

    @Override
    public List<float[]> embed(List<String> texts) {
        if (texts == null || texts.isEmpty()) return List.of();
        List<float[]> out = new ArrayList<>(texts.size());
        for (int i = 0; i < texts.size(); i += BATCH_SIZE) {
            List<String> slice = texts.subList(i, Math.min(i + BATCH_SIZE, texts.size()));
            out.addAll(callOnce(slice));
        }
        return out;
    }

    @Override
    public String modelId() { return model; }

    @SuppressWarnings("unchecked")
    private List<float[]> callOnce(List<String> batch) {
        Map<String, Object> body = Map.of(
                "model", model,
                "input", batch,
                "encoding_format", "float"
        );
        Map<String, Object> resp = client.post()
                .uri("/embeddings")
                .body(body)
                .retrieve()
                .body(Map.class);

        if (resp == null) throw new IllegalStateException("Empty embedding response");
        List<Map<String, Object>> data = (List<Map<String, Object>>) resp.get("data");
        if (data == null) throw new IllegalStateException("No data in embedding response: " + resp);

        List<float[]> vectors = new ArrayList<>(data.size());
        for (Map<String, Object> item : data) {
            List<Number> embedding = (List<Number>) item.get("embedding");
            float[] arr = new float[embedding.size()];
            for (int i = 0; i < embedding.size(); i++) arr[i] = embedding.get(i).floatValue();
            vectors.add(arr);
        }
        return vectors;
    }
}
