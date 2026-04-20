package com.erplite.inventory.rag.session;

import com.erplite.inventory.rag.dto.MessageResponse;
import com.erplite.inventory.rag.dto.SessionResponse;
import com.erplite.inventory.rag.dto.SourceRef;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatSessionService {

    private final ChatSessionRepository sessionRepo;
    private final ChatMessageRepository messageRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    @Transactional
    public ChatSession create(String userId, String title) {
        ChatSession s = ChatSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .userId(userId)
                .title(title)
                .build();
        return sessionRepo.save(s);
    }

    public ChatSession requireOwned(String sessionId, String userId) {
        return sessionRepo.findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
    }

    public List<SessionResponse> listByUser(String userId) {
        return sessionRepo.findByUserIdOrderByUpdatedDateDesc(userId).stream()
                .map(s -> new SessionResponse(s.getSessionId(), s.getTitle(),
                        s.getCreatedDate(), s.getUpdatedDate()))
                .toList();
    }

    public List<MessageResponse> listMessages(String sessionId, String userId) {
        requireOwned(sessionId, userId);
        return messageRepo.findBySessionIdOrderByCreatedDateAsc(sessionId).stream()
                .map(m -> new MessageResponse(
                        m.getMessageId(),
                        m.getRole().name(),
                        m.getContent(),
                        parseSources(m.getSources()),
                        m.getQueryLogId(),
                        m.getCreatedDate()
                ))
                .toList();
    }

    @Transactional
    public void delete(String sessionId, String userId) {
        requireOwned(sessionId, userId);
        sessionRepo.deleteById(sessionId);
    }

    @Transactional
    public void touch(String sessionId) {
        sessionRepo.findById(sessionId).ifPresent(s -> {
            s.setUpdatedDate(LocalDateTime.now());
            sessionRepo.save(s);
        });
    }

    private List<SourceRef> parseSources(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return mapper.readValue(json, new TypeReference<List<SourceRef>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
