package com.forma.api.controller;

import com.forma.api.dto.PostResponse;
import com.forma.api.service.PostService;
import com.forma.api.utils.exceptions.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ModeratorControllerTest {

    @Mock
    private PostService postService;

    @InjectMocks
    private ModeratorController moderatorController;

    private MockMvc mockMvc;
    private PostResponse flaggedPost;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(moderatorController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        flaggedPost = PostResponse.builder()
                .id(UUID.randomUUID())
                .title("Flagged Post")
                .body("Some toxic content")
                .authorUsername("badactor")
                .likeCount(0L)
                .aiFlagged(true)
                .flaggedMisleading(false)
                .aiReasoning("Contains hate speech and misinformation")
                .updatedAt(Instant.now())
                .build();
    }

    // --- GET ALL FLAGGED POSTS ---

    @Test
    void getAllFlaggedPosts_returns200WithList() throws Exception {
        when(postService.getAllFlaggedPosts()).thenReturn(List.of(flaggedPost));

        mockMvc.perform(get("/api/moderator"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Flagged Post"))
                .andExpect(jsonPath("$[0].aiFlagged").value(true))
                .andExpect(jsonPath("$[0].aiReasoning").value("Contains hate speech and misinformation"));
    }

    @Test
    void getAllFlaggedPosts_emptyQueue_returns200EmptyList() throws Exception {
        when(postService.getAllFlaggedPosts()).thenReturn(List.of());

        mockMvc.perform(get("/api/moderator"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getAllFlaggedPosts_multiplePosts_returnsAll() throws Exception {
        PostResponse second = PostResponse.builder()
                .id(UUID.randomUUID())
                .title("Another Flagged Post")
                .body("More bad content")
                .authorUsername("anotherbadactor")
                .likeCount(2L)
                .aiFlagged(true)
                .flaggedMisleading(false)
                .aiReasoning("Contains misinformation")
                .updatedAt(Instant.now())
                .build();

        when(postService.getAllFlaggedPosts()).thenReturn(List.of(flaggedPost, second));

        mockMvc.perform(get("/api/moderator"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
