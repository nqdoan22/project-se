package com.erplite.inventory.rag.dto;

import java.time.LocalDateTime;

public record SessionResponse(
        String sessionId,
        String title,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {}
