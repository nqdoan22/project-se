package com.erplite.inventory.rag.session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySessionIdOrderByCreatedDateAsc(String sessionId);

    List<ChatMessage> findTop8BySessionIdOrderByCreatedDateDesc(String sessionId);
}
