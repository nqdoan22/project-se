package com.erplite.inventory.rag;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(RagProperties.class)
public class RagConfig {

    @Bean("qdrantRestClient")
    public RestClient qdrantRestClient(RagProperties props) {
        return RestClient.builder()
                .baseUrl(props.qdrant().baseUrl())
                .defaultHeader("api-key", props.qdrant().apiKey() == null ? "" : props.qdrant().apiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean("openAiRestClient")
    public RestClient openAiRestClient(RagProperties props) {
        return RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + (props.openai().apiKey() == null ? "" : props.openai().apiKey()))
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
