package com.forma.api.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record PostResponse(
        UUID id,
        String title,
        String body,
        String authorUsername,
        long likeCount,
        boolean liked,
        boolean aiFlagged,
        double aiScore,
        boolean flaggedMisleading,
        Instant updatedAt,
        String aiReasoning) {}
