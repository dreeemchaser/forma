package com.forma.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "A comment as returned by the API")
public record CommentResponse(

        @Schema(description = "Unique comment ID", example = "c1000000-0000-0000-0000-000000000001")
        UUID id,

        @Schema(description = "Username of the comment author", example = "bob")
        String authorUsername,

        @Schema(description = "Comment text", example = "Would add Go to that list for backend services.")
        String body,

        @Schema(description = "Timestamp when the comment was created (ISO-8601)", example = "2026-04-24T09:30:00Z")
        Instant createdAt

) {}
