package com.erplite.inventory.rag.dto;

import java.time.LocalDateTime;
import java.util.List;

public record HealthResponse(
        List<TableStatus> tables,
        boolean qdrantOk,
        String qdrantMessage
) {
    public record TableStatus(
            String name,
            LocalDateTime lastModifiedAt,
            long rowsSynced,
            LocalDateTime lastRunAt,
            String lastError
    ) {}
}
