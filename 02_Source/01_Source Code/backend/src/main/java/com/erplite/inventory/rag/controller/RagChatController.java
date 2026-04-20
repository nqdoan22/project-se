package com.erplite.inventory.rag.controller;

import com.erplite.inventory.rag.dto.ChatRequest;
import com.erplite.inventory.rag.dto.ChatResponse;
import com.erplite.inventory.rag.dto.FeedbackRequest;
import com.erplite.inventory.rag.dto.MessageResponse;
import com.erplite.inventory.rag.dto.SessionResponse;
import com.erplite.inventory.rag.query.RagQueryService;
import com.erplite.inventory.rag.session.ChatSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rag")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RagChatController {

    private final RagQueryService queryService;
    private final ChatSessionService sessionService;

    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = userIdOf(jwt);
        return ResponseEntity.ok(queryService.answer(request.question(), request.sessionId(), userId));
    }

    @PostMapping("/feedback/{queryLogId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> feedback(
            @PathVariable Long queryLogId,
            @Valid @RequestBody FeedbackRequest body
    ) {
        queryService.recordFeedback(queryLogId, body.feedback());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sessions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SessionResponse>> listSessions(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(sessionService.listByUser(userIdOf(jwt)));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MessageResponse>> listMessages(
            @PathVariable String sessionId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(sessionService.listMessages(sessionId, userIdOf(jwt)));
    }

    @DeleteMapping("/sessions/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteSession(
            @PathVariable String sessionId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        sessionService.delete(sessionId, userIdOf(jwt));
        return ResponseEntity.noContent().build();
    }

    private static String userIdOf(Jwt jwt) {
        if (jwt == null) return "anonymous";
        String sub = jwt.getSubject();
        return sub != null ? sub : "anonymous";
    }
}
