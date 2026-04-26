package com.forma.api.controller;

import com.forma.api.dto.CreatePostRequest;
import com.forma.api.dto.CreateCommentRequest;
import com.forma.api.dto.CommentResponse;
import com.forma.api.dto.PostResponse;
import com.forma.api.model.User;
import com.forma.api.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "Create, read, like, comment on, and moderate posts. " +
        "Read endpoints are public. Write endpoints require authentication.")
public class PostController {

    private final PostService postService;

    // ── GET /api/posts ────────────────────────────────────────────────────────

    @Operation(
            summary = "Get all posts",
            description = "Returns all posts ordered by most recently updated (descending). " +
                    "If authenticated, the `liked` field reflects whether the current user has liked each post. " +
                    "If unauthenticated, `liked` is always `false`."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Posts returned (empty array if none exist)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PostResponse.class))))
    })
    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(postService.getAllPosts(user));
    }

    // ── GET /api/posts/{id} ───────────────────────────────────────────────────

    @Operation(
            summary = "Get post by ID",
            description = "Returns a single post by its UUID. " +
                    "If authenticated, the `liked` field reflects whether the current user has liked this post."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "404", description = "Post not found",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Post not found: b1000000-0000-0000-0000-000000000001")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(
            @Parameter(description = "UUID of the post", example = "b1000000-0000-0000-0000-000000000001")
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(postService.getPostById(id, user));
    }

    // ── POST /api/posts ───────────────────────────────────────────────────────

    @Operation(
            summary = "Create a post",
            description = "Creates a new post. The content is automatically analysed by Claude AI before the post is saved. " +
                    "If the AI score exceeds `0.6`, `aiFlagged` is set to `true` and the post appears in the moderation queue. " +
                    "The `aiScore` and `aiReasoning` fields are always populated in the response.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Post created — AI analysis included in response",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error — title or body failed constraints",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Title is required"))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Unauthorized")))
    })
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(request, user));
    }

    // ── GET /api/posts/{id}/comments ─────────────────────────────────────────

    @Operation(
            summary = "Get comments for a post",
            description = "Returns all comments on a post, ordered by creation time ascending (oldest first)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comments returned (empty array if none)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = CommentResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Post not found",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Post not found: b1000000-0000-0000-0000-000000000001")))
    })
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(
            @Parameter(description = "UUID of the post", example = "b1000000-0000-0000-0000-000000000001")
            @PathVariable UUID id) {
        return ResponseEntity.ok(postService.getComments(id));
    }

    // ── POST /api/posts/{id}/comments ────────────────────────────────────────

    @Operation(
            summary = "Add a comment to a post",
            description = "Adds a comment to an existing post. Any authenticated user may comment on any post, including their own.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Comment created",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error — body is blank or too long",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Comment body is required"))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Unauthorized"))),
            @ApiResponse(responseCode = "404", description = "Post not found",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Post not found: b1000000-0000-0000-0000-000000000001")))
    })
    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @Parameter(description = "UUID of the post to comment on", example = "b1000000-0000-0000-0000-000000000001")
            @PathVariable UUID id,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.addComment(id, request, user));
    }

    // ── POST /api/posts/{id}/like ─────────────────────────────────────────────

    @Operation(
            summary = "Like a post",
            description = "Likes a post on behalf of the authenticated user. " +
                    "Returns `409` if the user has already liked the post or if they are the post author (self-likes not allowed).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Post liked successfully — no body returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Unauthorized"))),
            @ApiResponse(responseCode = "404", description = "Post not found",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Post not found: b1000000-0000-0000-0000-000000000001"))),
            @ApiResponse(responseCode = "409", description = "Conflict — already liked, or attempting to like own post",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "You have already liked this post")))
    })
    @PostMapping("/{id}/like")
    public ResponseEntity<Void> likePost(
            @Parameter(description = "UUID of the post to like", example = "b1000000-0000-0000-0000-000000000001")
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        postService.likePost(id, user);
        return ResponseEntity.noContent().build();
    }

    // ── DELETE /api/posts/{id}/like ───────────────────────────────────────────

    @Operation(
            summary = "Unlike a post",
            description = "Removes the authenticated user's like from a post. Returns `409` if the post was not previously liked.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Like removed — no body returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Unauthorized"))),
            @ApiResponse(responseCode = "409", description = "Post was not liked by this user",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "You have not liked this post")))
    })
    @DeleteMapping("/{id}/like")
    public ResponseEntity<Void> unlikePost(
            @Parameter(description = "UUID of the post to unlike", example = "b1000000-0000-0000-0000-000000000001")
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        postService.unlikePost(id, user);
        return ResponseEntity.noContent().build();
    }

    // ── POST /api/posts/{id}/flag ─────────────────────────────────────────────

    @Operation(
            summary = "Confirm post as misleading",
            description = "Sets `flaggedMisleading: true` on a post, confirming the AI's flag. " +
                    "This is a **moderator-only** action. The post remains visible but is marked in the feed. " +
                    "Use `DELETE /api/posts/{id}/flag` to reverse this decision.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Post confirmed as misleading — no body returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Unauthorized"))),
            @ApiResponse(responseCode = "403", description = "Forbidden — caller does not have the MODERATOR role",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Forbidden"))),
            @ApiResponse(responseCode = "404", description = "Post not found",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Post not found: b1000000-0000-0000-0000-000000000001")))
    })
    @PostMapping("/{id}/flag")
    public ResponseEntity<Void> flagPost(
            @Parameter(description = "UUID of the post to flag as misleading", example = "b1000000-0000-0000-0000-000000000006")
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        postService.flagPost(id);
        return ResponseEntity.noContent().build();
    }

    // ── DELETE /api/posts/{id}/flag ───────────────────────────────────────────

    @Operation(
            summary = "Dismiss misleading flag",
            description = "Removes a post from the moderation queue by setting `aiFlagged: false` and `flaggedMisleading: false`. " +
                    "Use this when the AI flag is a false positive. This is a **moderator-only** action.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Flag dismissed — post removed from moderation queue. No body returned."),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Unauthorized"))),
            @ApiResponse(responseCode = "403", description = "Forbidden — caller does not have the MODERATOR role",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Forbidden"))),
            @ApiResponse(responseCode = "404", description = "Post not found",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Post not found: b1000000-0000-0000-0000-000000000006")))
    })
    @DeleteMapping("/{id}/flag")
    public ResponseEntity<Void> unflagPost(
            @Parameter(description = "UUID of the post to dismiss the flag on", example = "b1000000-0000-0000-0000-000000000006")
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        postService.unflagPost(id);
        return ResponseEntity.noContent().build();
    }
}
