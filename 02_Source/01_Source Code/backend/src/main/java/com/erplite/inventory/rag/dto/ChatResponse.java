package com.erplite.inventory.rag.dto;

import java.util.List;

public record ChatResponse(
        String answer,
        List<SourceRef> sources,
        Long queryLogId,
        String sessionId
) {}
