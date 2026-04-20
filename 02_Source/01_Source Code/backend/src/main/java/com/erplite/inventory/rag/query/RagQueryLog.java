package com.erplite.inventory.rag.query;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rag_query_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RagQueryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", length = 36)
    private String userId;

    @Column(name = "question", columnDefinition = "TEXT", nullable = false)
    private String question;

    @Column(name = "retrieved_ids", columnDefinition = "JSON")
    private String retrievedIds;

    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback", length = 4)
    private Feedback feedback;

    @Column(name = "asked_at", nullable = false)
    private LocalDateTime askedAt;

    @PrePersist
    void onCreate() {
        if (askedAt == null) askedAt = LocalDateTime.now();
    }

    public enum Feedback { up, down }
}
