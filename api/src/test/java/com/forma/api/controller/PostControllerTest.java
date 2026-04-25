package com.forma.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forma.api.dto.CommentResponse;
import com.forma.api.dto.CreateCommentRequest;
import com.forma.api.dto.CreatePostRequest;
import com.forma.api.dto.PostResponse;
import com.forma.api.model.Role;
import com.forma.api.model.User;
import com.forma.api.service.PostService;
import com.forma.api.utils.exceptions.ConflictException;
import com.forma.api.utils.exceptions.GlobalExceptionHandler;
import com.forma.api.utils.exceptions.PostNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    @Mock
    private PostService postService;

    @InjectMocks
    private PostController postController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UUID postId;
    private PostResponse mockPostResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(postController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        postId = UUID.randomUUID();

        mockPostResponse = PostResponse.builder()
                .id(postId)
                .title("Test Title")
                .body("Test Body")
                .authorUsername("testuser")
                .likeCount(0L)
                .aiFlagged(false)
                .flaggedMisleading(false)
                .aiReasoning("Clean post")
                .updatedAt(Instant.now())
                .build();
    }

    // --- GET ALL POSTS ---

    @Test
    void getAllPosts_returns200WithList() throws Exception {
        when(postService.getAllPosts(any())).thenReturn(List.of(mockPostResponse));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Title"))
                .andExpect(jsonPath("$[0].authorUsername").value("testuser"));
    }

    @Test
    void getAllPosts_emptyList_returns200() throws Exception {
        when(postService.getAllPosts(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // --- GET POST BY ID ---

    @Test
    void getPostById_found_returns200() throws Exception {
        when(postService.getPostById(eq(postId), any())).thenReturn(mockPostResponse);

        mockMvc.perform(get("/api/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Title"));
    }

    @Test
    void getPostById_notFound_returns404() throws Exception {
        when(postService.getPostById(eq(postId), any())).thenThrow(new PostNotFoundException(postId.toString()));

        mockMvc.perform(get("/api/posts/{id}", postId))
                .andExpect(status().isNotFound());
    }

    // --- CREATE POST ---

    @Test
    void createPost_returns201() throws Exception {
        CreatePostRequest request = new CreatePostRequest("Test Title", "Test Body");
        when(postService.createPost(any(CreatePostRequest.class), any(User.class)))
                .thenReturn(mockPostResponse);

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Title"));
    }

    @Test
    void createPost_flaggedPost_returnsAiFlaggedTrue() throws Exception {
        PostResponse flaggedResponse = PostResponse.builder()
                .id(postId)
                .title("Bad Title")
                .body("Bad Body")
                .authorUsername("testuser")
                .likeCount(0L)
                .aiFlagged(true)
                .aiReasoning("Toxic content detected")
                .flaggedMisleading(false)
                .updatedAt(Instant.now())
                .build();

        when(postService.createPost(any(), any())).thenReturn(flaggedResponse);

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreatePostRequest("Bad Title", "Bad Body"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.aiFlagged").value(true))
                .andExpect(jsonPath("$.aiReasoning").value("Toxic content detected"));
    }

    // --- LIKE POST ---

    @Test
    void likePost_returns204() throws Exception {
        doNothing().when(postService).likePost(eq(postId), any());

        mockMvc.perform(post("/api/posts/{id}/like", postId))
                .andExpect(status().isNoContent());
    }

    @Test
    void likePost_ownPost_returns409() throws Exception {
        doThrow(new ConflictException("You are not allowed to like your own post"))
                .when(postService).likePost(eq(postId), any());

        mockMvc.perform(post("/api/posts/{id}/like", postId))
                .andExpect(status().isConflict());
    }

    @Test
    void likePost_alreadyLiked_returns409() throws Exception {
        doThrow(new ConflictException("You have already liked this post"))
                .when(postService).likePost(eq(postId), any());

        mockMvc.perform(post("/api/posts/{id}/like", postId))
                .andExpect(status().isConflict());
    }

    // --- UNLIKE POST ---

    @Test
    void unlikePost_returns204() throws Exception {
        doNothing().when(postService).unlikePost(eq(postId), any());

        mockMvc.perform(delete("/api/posts/{id}/like", postId))
                .andExpect(status().isNoContent());
    }

    @Test
    void unlikePost_notLiked_returns409() throws Exception {
        doThrow(new ConflictException("You have not liked this post"))
                .when(postService).unlikePost(eq(postId), any());

        mockMvc.perform(delete("/api/posts/{id}/like", postId))
                .andExpect(status().isConflict());
    }

    // --- ADD COMMENT ---

    @Test
    void addComment_returns201() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest("Great post!");
        CommentResponse commentResponse = new CommentResponse(
                UUID.randomUUID(), "testuser", "Great post!", Instant.now());

        when(postService.addComment(eq(postId), any(CreateCommentRequest.class), any()))
                .thenReturn(commentResponse);

        mockMvc.perform(post("/api/posts/{id}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.body").value("Great post!"));
    }

    @Test
    void addComment_postNotFound_returns404() throws Exception {
        when(postService.addComment(eq(postId), any(), any()))
                .thenThrow(new PostNotFoundException(postId.toString()));

        mockMvc.perform(post("/api/posts/{id}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"hello\"}"))
                .andExpect(status().isNotFound());
    }

    // --- FLAG POST ---

    @Test
    void flagPost_returns204() throws Exception {
        doNothing().when(postService).flagPost(postId);

        mockMvc.perform(post("/api/posts/{id}/flag", postId))
                .andExpect(status().isNoContent());
    }

    @Test
    void flagPost_postNotFound_returns404() throws Exception {
        doThrow(new PostNotFoundException(postId.toString())).when(postService).flagPost(postId);

        mockMvc.perform(post("/api/posts/{id}/flag", postId))
                .andExpect(status().isNotFound());
    }

    // --- UNFLAG POST ---

    @Test
    void unflagPost_returns204() throws Exception {
        doNothing().when(postService).unflagPost(postId);

        mockMvc.perform(delete("/api/posts/{id}/flag", postId))
                .andExpect(status().isNoContent());
    }
}
