package com.forma.api.controller;

import com.forma.api.dto.PostResponse;
import com.forma.api.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/moderator")
@RequiredArgsConstructor
@Tag(name = "Moderator", description = "Endpoints exclusively for users with the **MODERATOR** role. " +
        "All routes here require a valid JWT token from a moderator account. " +
        "To confirm or dismiss a flag, use `POST /api/posts/{id}/flag` and `DELETE /api/posts/{id}/flag`.")
public class ModeratorController {

    private final PostService postService;

    @Operation(
            summary = "Get moderation queue",
            description = "Returns all posts that were automatically flagged by Claude AI with a score above `0.6`, " +
                    "ordered by most recently updated. Each post includes the full `aiScore` and `aiReasoning` so the " +
                    "moderator can make an informed decision. " +
                    "\n\n**Actions available per post:**" +
                    "\n- `POST /api/posts/{id}/flag` — confirm the post is misleading" +
                    "\n- `DELETE /api/posts/{id}/flag` — dismiss the flag (false positive)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Moderation queue returned (empty array if queue is clear)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PostResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Unauthorized"))),
            @ApiResponse(responseCode = "403", description = "Forbidden — caller does not have the MODERATOR role",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Forbidden")))
    })
    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllFlaggedPosts() {
        return ResponseEntity.ok(postService.getAllFlaggedPosts());
    }

}
