package com.forma.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Schema(description = "Payload for creating a new post")
@Builder
public record CreatePostRequest(

        @Schema(
                description = "Post title — short and descriptive",
                example = "My morning coffee routine",
                maxLength = 255
        )
        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        @Schema(
                description = "Post body — the main content",
                example = "Started my day with a double espresso and a quick walk. Small habits really do make a difference.",
                maxLength = 255
        )
        @NotBlank(message = "Body is required")
        @Size(max = 255, message = "Body must not exceed 255 characters")
        String body

) {}
