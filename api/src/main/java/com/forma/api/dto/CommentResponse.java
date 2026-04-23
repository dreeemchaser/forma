package com.forma.api.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        String authorUsername,
        String body,
        Instant createdAt
) {}
