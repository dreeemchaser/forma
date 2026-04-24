package com.forma.api.controller;

import com.forma.api.dto.PostResponse;
import com.forma.api.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/moderator")
@RequiredArgsConstructor
@Tag(name = "Moderator", description = "Moderator-only endpoints for reviewing AI flagged posts")
public class ModeratorController {

    private final PostService postService;

    @Operation(summary = "Get all AI flagged posts",
            description = "Returns all posts flagged by Claude with a score above 0.6. Moderator only.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Flagged posts retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Moderators only")
    })
    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllFlaggedPosts() {
        return ResponseEntity.ok(postService.getAllFlaggedPosts());
    }

}
