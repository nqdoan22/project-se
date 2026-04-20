package com.erplite.inventory.rag;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rag")
public record RagProperties(Sync sync, Qdrant qdrant, OpenAi openai) {

    public record Sync(
            boolean enabled,
            long intervalMs,
            int batchSize
    ) {}

    public record Qdrant(
            String host,
            int port,
            boolean useTls,
            String apiKey,
            String collection
    ) {
        public String baseUrl() {
            String scheme = useTls ? "https" : "http";
            return scheme + "://" + host + ":" + port;
        }
    }

    public record OpenAi(
            String apiKey,
            String embeddingModel,
            String chatModel
    ) {}
}
