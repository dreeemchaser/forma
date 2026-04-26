package com.forma.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "A post as returned by the API")
@Builder
public record PostResponse(

        @Schema(description = "Unique post ID", example = "b1000000-0000-0000-0000-000000000001")
        UUID id,

        @Schema(description = "Post title", example = "Thoughts on remote work in 2026")
        String title,

        @Schema(description = "Post body / main content",
                example = "Three years into fully remote work and I still think the async communication model is underrated.")
        String body,

        @Schema(description = "Username of the post author", example = "alice")
        String authorUsername,

        @Schema(description = "Total number of likes this post has received", example = "12")
        long likeCount,

        @Schema(description = "Whether the currently authenticated user has liked this post. Always false for unauthenticated requests.",
                example = "false")
        boolean liked,

        @Schema(description = "Whether this post was automatically flagged by Claude AI as potentially problematic (score > 0.6)",
                example = "false")
        boolean aiFlagged,

        @Schema(description = "Claude AI confidence score for this post being problematic. Range: 0.0 (clean) to 1.0 (very likely problematic).",
                example = "0.08", minimum = "0.0", maximum = "1.0")
        double aiScore,

        @Schema(description = "Whether a moderator has confirmed this post as misleading",
                example = "false")
        boolean flaggedMisleading,

        @Schema(description = "Timestamp of the last update to this post (ISO-8601)",
                example = "2026-04-24T10:15:30Z")
        Instant updatedAt,

        @Schema(description = "Claude AI's reasoning for the assigned score. Explains what was detected (or not detected) in the content.",
                example = "Thoughtful opinion piece. No toxicity, misinformation, or manipulative language detected.")
        String aiReasoning

) {}
