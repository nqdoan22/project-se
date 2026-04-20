package com.erplite.inventory.rag.sync;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rag_embed_log",
        uniqueConstraints = @UniqueConstraint(name = "uk_embed_source_pk",
                columnNames = {"source_table", "source_pk"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RagEmbedLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_table", length = 64, nullable = false)
    private String sourceTable;

    @Column(name = "source_pk", length = 36, nullable = false)
    private String sourcePk;

    @Column(name = "content_hash", length = 64, nullable = false)
    private String contentHash;

    @Column(name = "embedded_at", nullable = false)
    private LocalDateTime embeddedAt;

    @Column(name = "model", length = 100, nullable = false)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10, nullable = false)
    private Status status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @PrePersist
    void onCreate() {
        if (embeddedAt == null) embeddedAt = LocalDateTime.now();
    }

    public enum Status { OK, SKIPPED, FAILED }
}
