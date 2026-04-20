package com.erplite.inventory.rag.embed;

import java.util.List;

public interface EmbeddingClient {

    /** Embed a batch of texts. Returns vectors in the same order. */
    List<float[]> embed(List<String> texts);

    /** Embed a single text. */
    default float[] embed(String text) {
        return embed(List.of(text)).get(0);
    }

    /** Model identifier (for logging / audit). */
    String modelId();
}
