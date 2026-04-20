package com.erplite.inventory.rag.store;

import java.util.List;
import java.util.Map;

public interface VectorStore {

    /** Upsert a batch of points. Point ID, vector, and payload required. */
    void upsert(List<Point> points);

    /** Search by vector. Filter map may be null or empty. Returns top-k hits. */
    List<Hit> search(float[] vector, Map<String, Object> filterMust, int limit);

    /** Delete points by Qdrant ID. */
    void delete(List<String> pointIds);

    /** Delete all points in the collection (used by admin resync). */
    void deleteAll();

    /** Ensure the collection exists (create if missing). */
    void ensureCollection(int vectorSize);

    /** Health check — returns true if reachable. */
    boolean ping();

    record Point(String id, float[] vector, Map<String, Object> payload) {}

    record Hit(String id, double score, Map<String, Object> payload) {}
}
