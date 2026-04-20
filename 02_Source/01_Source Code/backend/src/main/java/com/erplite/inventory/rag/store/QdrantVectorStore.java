package com.erplite.inventory.rag.store;

import com.erplite.inventory.rag.RagProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class QdrantVectorStore implements VectorStore {

    private final RestClient client;
    private final String collection;

    public QdrantVectorStore(@Qualifier("qdrantRestClient") RestClient client, RagProperties props) {
        this.client = client;
        this.collection = props.qdrant().collection();
    }

    @Override
    public void upsert(List<Point> points) {
        if (points == null || points.isEmpty()) return;
        List<Map<String, Object>> pts = new ArrayList<>(points.size());
        for (Point p : points) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", p.id());
            item.put("vector", toList(p.vector()));
            item.put("payload", p.payload());
            pts.add(item);
        }
        Map<String, Object> body = Map.of("points", pts);
        client.put()
                .uri("/collections/{c}/points?wait=true", collection)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Hit> search(float[] vector, Map<String, Object> filterMust, int limit) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("vector", toList(vector));
        body.put("limit", limit);
        body.put("with_payload", true);
        if (filterMust != null && !filterMust.isEmpty()) {
            List<Map<String, Object>> must = new ArrayList<>();
            filterMust.forEach((k, v) -> {
                Map<String, Object> cond = new LinkedHashMap<>();
                cond.put("key", k);
                Map<String, Object> match = new LinkedHashMap<>();
                match.put("value", v);
                cond.put("match", match);
                must.add(cond);
            });
            body.put("filter", Map.of("must", must));
        }

        Map<String, Object> resp = client.post()
                .uri("/collections/{c}/points/search", collection)
                .body(body)
                .retrieve()
                .body(Map.class);
        if (resp == null) return List.of();
        List<Map<String, Object>> result = (List<Map<String, Object>>) resp.get("result");
        if (result == null) return List.of();
        List<Hit> hits = new ArrayList<>(result.size());
        for (Map<String, Object> r : result) {
            String id = String.valueOf(r.get("id"));
            double score = ((Number) r.getOrDefault("score", 0)).doubleValue();
            Map<String, Object> payload = (Map<String, Object>) r.getOrDefault("payload", Map.of());
            hits.add(new Hit(id, score, payload));
        }
        return hits;
    }

    @Override
    public void delete(List<String> pointIds) {
        if (pointIds == null || pointIds.isEmpty()) return;
        client.post()
                .uri("/collections/{c}/points/delete?wait=true", collection)
                .body(Map.of("points", pointIds))
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void deleteAll() {
        // Delete all by clearing with a match-all filter (empty must).
        client.post()
                .uri("/collections/{c}/points/delete?wait=true", collection)
                .body(Map.of("filter", Map.of("must", List.of())))
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void ensureCollection(int vectorSize) {
        // Check if collection exists; create if not.
        try {
            client.get()
                    .uri("/collections/{c}", collection)
                    .retrieve()
                    .toBodilessEntity();
            return; // exists
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() != 404) throw e;
        }

        Map<String, Object> body = Map.of(
                "vectors", Map.of("size", vectorSize, "distance", "Cosine")
        );
        try {
            client.put()
                    .uri("/collections/{c}", collection)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Created Qdrant collection {} (size={}, cosine)", collection, vectorSize);
        } catch (RestClientResponseException e) {
            log.warn("Failed to create Qdrant collection {}: {}", collection, e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean ping() {
        try {
            HttpStatusCode code = client.get().uri("/collections/{c}", collection)
                    .retrieve().toBodilessEntity().getStatusCode();
            return code.is2xxSuccessful();
        } catch (Exception e) {
            log.debug("Qdrant ping failed: {}", e.getMessage());
            return false;
        }
    }

    private static List<Float> toList(float[] v) {
        List<Float> list = new ArrayList<>(v.length);
        for (float f : v) list.add(f);
        return list;
    }
}
