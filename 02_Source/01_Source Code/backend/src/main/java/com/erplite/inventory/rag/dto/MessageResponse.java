package com.erplite.inventory.rag.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MessageResponse(
        Long messageId,
        String role,
        String content,
        List<SourceRef> sources,
        Long queryLogId,
        LocalDateTime createdDate
) {}
