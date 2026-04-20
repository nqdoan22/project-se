package com.erplite.inventory.rag.sync;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rag_sync_state")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RagSyncState {

    @Id
    @Column(name = "source_table", length = 64)
    private String sourceTable;

    @Column(name = "last_modified_at", nullable = false)
    private LocalDateTime lastModifiedAt;

    @Column(name = "last_pk", length = 36)
    private String lastPk;

    @Column(name = "rows_synced", nullable = false)
    private long rowsSynced;

    @Column(name = "last_run_at")
    private LocalDateTime lastRunAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;
}
