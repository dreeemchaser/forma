package com.forma.api.controller;

import com.forma.api.dto.CreatePostRequest;
import com.forma.api.dto.CreateCommentRequest;
import com.forma.api.dto.CommentResponse;
import com.forma.api.dto.PostResponse;
import com.forma.api.model.User;
import com.forma.api.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "Post management endpoints")
public class PostController {

    private final PostService postService;

    // GET /api/posts
    @Operation(summary = "Get all posts", description = "Returns all posts ordered by most recently updated")
    @ApiResponse(responseCode = "200", description = "Posts retrieved successfully")
    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(postService.getAllPosts(user));
    }

    // GET /api/posts/{id}
    @Operation(summary = "Get post by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post found"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(postService.getPostById(id, user));
    }

    // POST /api/posts
    @Operation(summary = "Create a post", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Post created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody CreatePostRequest request,
                                                   @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(request, user));
    }

    // GET /api/posts/{id}/comments
    @Operation(summary = "Get comments for a post")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable UUID id) {
        return ResponseEntity.ok(postService.getComments(id));
    }

    // POST /api/posts/{id}/comments
    @Operation(summary = "Add a comment to a post", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Comment added successfully"),
            @ApiResponse(responseCode = "404", description = "Post not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> addComment(@PathVariable UUID id,
                                                      @Valid @RequestBody CreateCommentRequest request,
                                                      @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.addComment(id, request, user));
    }

    // POST /api/posts/{id}/like
    @Operation(summary = "Like a post", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Post liked successfully"),
            @ApiResponse(responseCode = "404", description = "Post not found"),
            @ApiResponse(responseCode = "409", description = "Already liked or own post")
    })
    @PostMapping("/{id}/like")
    public ResponseEntity<Void> likePost(@PathVariable UUID id,
                                         @AuthenticationPrincipal User user) {
        postService.likePost(id, user);
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/posts/{id}/like
    @Operation(summary = "Unlike a post", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Post unliked successfully"),
            @ApiResponse(responseCode = "409", description = "Post not liked")
    })
    @DeleteMapping("/{id}/like")
    public ResponseEntity<Void> unlikePost(@PathVariable UUID id,
                                           @AuthenticationPrincipal User user) {
        postService.unlikePost(id, user);
        return ResponseEntity.noContent().build();
    }

    // POST /api/posts/{id}/flag
    @Operation(summary = "Flag a post as misleading", description = "Moderator only",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Post flagged successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Moderators only"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @PostMapping("/{id}/flag")
    public ResponseEntity<Void> flagPost(@PathVariable UUID id,
                                         @AuthenticationPrincipal User user) {
        postService.flagPost(id);
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/posts/{id}/flag
    @Operation(summary = "Remove flag from a post", description = "Moderator only",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Flag removed successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Moderators only"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @DeleteMapping("/{id}/flag")
    public ResponseEntity<Void> unflagPost(@PathVariable UUID id,
                                           @AuthenticationPrincipal User user) {
        postService.unflagPost(id);
        return ResponseEntity.noContent().build();
    }
}
