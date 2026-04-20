package com.erplite.inventory.rag.dto;

import jakarta.validation.constraints.Pattern;

public record FeedbackRequest(
        @Pattern(regexp = "up|down", message = "feedback must be 'up' or 'down'")
        String feedback
) {}
