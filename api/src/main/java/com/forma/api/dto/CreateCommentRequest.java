package com.forma.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for adding a comment to a post")
public record CreateCommentRequest(

        @Schema(
                description = "The comment text",
                example = "Fully agree on async — Notion + Slack threads changed everything for our team.",
                maxLength = 500
        )
        @NotBlank(message = "Comment body is required")
        @Size(max = 500, message = "Comment must not exceed 500 characters")
        String body

) {}
