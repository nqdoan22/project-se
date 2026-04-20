package com.erplite.inventory.rag.session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, String> {

    List<ChatSession> findByUserIdOrderByUpdatedDateDesc(String userId);

    Optional<ChatSession> findBySessionIdAndUserId(String sessionId, String userId);
}
