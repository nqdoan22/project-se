package com.erplite.inventory.rag.query;

import com.erplite.inventory.rag.dto.ChatResponse;
import com.erplite.inventory.rag.dto.SourceRef;
import com.erplite.inventory.rag.embed.EmbeddingClient;
import com.erplite.inventory.rag.session.ChatMessage;
import com.erplite.inventory.rag.session.ChatMessageRepository;
import com.erplite.inventory.rag.session.ChatSession;
import com.erplite.inventory.rag.session.ChatSessionService;
import com.erplite.inventory.rag.store.VectorStore;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagQueryService {

    private static final int RETRIEVAL_LIMIT = 8;

    private final EmbeddingClient embeddingClient;
    private final VectorStore vectorStore;
    private final LlmClient llmClient;
    private final PrefilterExtractor prefilter;
    private final PromptBuilder promptBuilder;
    private final RagQueryLogRepository queryLogRepo;
    private final ChatSessionService sessionService;
    private final ChatMessageRepository messageRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    public ChatResponse answer(String question, String sessionIdOrNull, String userId) {
        long start = System.currentTimeMillis();

        ChatSession session = sessionIdOrNull == null || sessionIdOrNull.isBlank()
                ? sessionService.create(userId, truncateTitle(question))
                : sessionService.requireOwned(sessionIdOrNull, userId);

        // 1. prefilter
        PrefilterExtractor.Filters filters = prefilter.extract(question);

        // 2. embed
        float[] qVec = embeddingClient.embed(question);

        // 3. retrieve (with fallback if filter gives nothing)
        List<VectorStore.Hit> hits = vectorStore.search(qVec, filters.must(), RETRIEVAL_LIMIT);
        if (hits.isEmpty() && !filters.must().isEmpty()) {
            log.debug("RAG: retry search without filter — primary returned 0 hits");
            hits = vectorStore.search(qVec, Map.of(), RETRIEVAL_LIMIT);
        }

        // 4. history (last 4 pairs)
        List<Map<String, String>> history = loadHistory(session.getSessionId());

        // 5. prompt + LLM
        String answer;
        try {
            List<Map<String, String>> messages = promptBuilder.build(question, hits, history);
            answer = llmClient.complete(messages, 0.1, 500);
        } catch (Exception e) {
            log.error("RAG: LLM completion failed: {}", e.getMessage());
            answer = "Xin lỗi, dịch vụ AI tạm thời không khả dụng. Vui lòng thử lại sau.";
        }

        // 6. build sources
        List<SourceRef> sources = new ArrayList<>(hits.size());
        for (VectorStore.Hit h : hits) {
            sources.add(new SourceRef(
                    String.valueOf(h.payload().getOrDefault("source_table", "?")),
                    String.valueOf(h.payload().getOrDefault("source_pk", "?")),
                    h.score()
            ));
        }

        int latency = (int) (System.currentTimeMillis() - start);

        // 7. persist query log + messages
        RagQueryLog qLog = RagQueryLog.builder()
                .userId(userId)
                .question(question)
                .retrievedIds(toJson(sources))
                .answer(answer)
                .latencyMs(latency)
                .build();
        queryLogRepo.save(qLog);

        messageRepo.save(ChatMessage.builder()
                .sessionId(session.getSessionId())
                .role(ChatMessage.Role.user)
                .content(question)
                .build());
        messageRepo.save(ChatMessage.builder()
                .sessionId(session.getSessionId())
                .role(ChatMessage.Role.assistant)
                .content(answer)
                .sources(toJson(sources))
                .queryLogId(qLog.getId())
                .build());

        sessionService.touch(session.getSessionId());

        return new ChatResponse(answer, sources, qLog.getId(), session.getSessionId());
    }

    public void recordFeedback(Long queryLogId, String feedback) {
        queryLogRepo.findById(queryLogId).ifPresent(l -> {
            l.setFeedback(RagQueryLog.Feedback.valueOf(feedback));
            queryLogRepo.save(l);
        });
    }

    private List<Map<String, String>> loadHistory(String sessionId) {
        List<ChatMessage> recent = messageRepo.findTop8BySessionIdOrderByCreatedDateDesc(sessionId);
        Collections.reverse(recent);
        List<Map<String, String>> out = new ArrayList<>();
        for (ChatMessage m : recent) {
            out.add(Map.of("role", m.getRole().name(), "content", m.getContent()));
        }
        return out;
    }

    private String toJson(Object o) {
        try {
            return mapper.writeValueAsString(o);
        } catch (JacksonException e) {
            return null;
        }
    }

    private static String truncateTitle(String q) {
        if (q == null) return "Cuộc trò chuyện";
        String s = q.trim();
        return s.length() > 80 ? s.substring(0, 80) + "…" : s;
    }
}
