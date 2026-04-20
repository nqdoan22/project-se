package com.erplite.inventory.rag.dto;

public record SourceRef(
        String sourceTable,
        String sourcePk,
        double score
) {}
