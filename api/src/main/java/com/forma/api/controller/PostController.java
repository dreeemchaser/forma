package com.forma.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    // GET /api/posts
    @GetMapping
    public ResponseEntity<?> getAllPosts() {
        return null;
    }

    // GET /api/posts/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(@PathVariable UUID id) {
        return null;
    }

    // POST /api/posts
    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody Object request,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        return null;
    }

    // POST /api/posts/{id}/comments
    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(@PathVariable UUID id,
                                        @RequestBody Object request,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        return null;
    }

    // POST /api/posts/{id}/like
    @PostMapping("/{id}/like")
    public ResponseEntity<?> likePost(@PathVariable UUID id,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        return null;
    }

    // DELETE /api/posts/{id}/like
    @DeleteMapping("/{id}/like")
    public ResponseEntity<?> unlikePost(@PathVariable UUID id,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        return null;
    }

    // POST /api/posts/{id}/flag
    @PostMapping("/{id}/flag")
    public ResponseEntity<?> flagPost(@PathVariable UUID id,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        return null;
    }

    // DELETE /api/posts/{id}/flag
    @DeleteMapping("/{id}/flag")
    public ResponseEntity<?> unflagPost(@PathVariable UUID id,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        return null;
    }
}