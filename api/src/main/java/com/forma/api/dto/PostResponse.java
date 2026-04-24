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
        boolean aiFlagged,
        boolean flaggedMisleading,
        Instant updatedAt,
        String aiReasoning) {}
