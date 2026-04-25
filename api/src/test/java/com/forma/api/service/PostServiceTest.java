package com.forma.api.service;

import com.forma.api.dto.*;
import com.forma.api.model.*;
import com.forma.api.repository.CommentRepository;
import com.forma.api.repository.PostLikeRepository;
import com.forma.api.repository.PostRepository;
import com.forma.api.utils.exceptions.ConflictException;
import com.forma.api.utils.exceptions.PostNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock private PostRepository postRepository;
    @Mock private PostLikeRepository postLikeRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private PostAnalysisInterface moderatorService;

    @InjectMocks
    private PostService postService;

    private User author;
    private User otherUser;
    private Post post;
    private UUID postId;

    @BeforeEach
    void setUp() {
        postId = UUID.randomUUID();

        author = User.builder()
                .id(UUID.randomUUID())
                .username("author")
                .password("pass")
                .role(Role.REGULAR_USER)
                .build();

        otherUser = User.builder()
                .id(UUID.randomUUID())
                .username("other")
                .password("pass")
                .role(Role.REGULAR_USER)
                .build();

        post = Post.builder()
                .id(postId)
                .title("Test Title")
                .body("Test Body")
                .postAuthor(author)
                .aiFlagged(false)
                .aiScore(0.2)
                .aiReasoning("Clean post")
                .flaggedMisleading(false)
                .updatedAt(Instant.now())
                .build();
    }

    // --- CREATE POST ---

    @Test
    void createPost_cleanPost_savesWithAiFlaggedFalse() {
        CreatePostRequest request = new CreatePostRequest("Title", "Body");
        PostAnalysisResponse analysis = new PostAnalysisResponse(0.2, "Looks clean");

        when(moderatorService.analysePost(request)).thenReturn(analysis);
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(postLikeRepository.countByPostId(any())).thenReturn(0L);

        PostResponse response = postService.createPost(request, author);

        assertThat(response.aiFlagged()).isFalse();
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void createPost_highScore_savesWithAiFlaggedTrue() {
        CreatePostRequest request = new CreatePostRequest("Bad Title", "Bad Body");
        PostAnalysisResponse analysis = new PostAnalysisResponse(0.9, "Very toxic");

        Post flaggedPost = Post.builder()
                .id(postId)
                .title("Bad Title")
                .body("Bad Body")
                .postAuthor(author)
                .aiFlagged(true)
                .aiScore(0.9)
                .aiReasoning("Very toxic")
                .updatedAt(Instant.now())
                .build();

        when(moderatorService.analysePost(request)).thenReturn(analysis);
        when(postRepository.save(any(Post.class))).thenReturn(flaggedPost);
        when(postLikeRepository.countByPostId(any())).thenReturn(0L);

        PostResponse response = postService.createPost(request, author);

        assertThat(response.aiFlagged()).isTrue();
    }

    @Test
    void createPost_scoreExactly06_notFlagged() {
        CreatePostRequest request = new CreatePostRequest("Title", "Body");
        PostAnalysisResponse analysis = new PostAnalysisResponse(0.6, "Borderline");

        when(moderatorService.analysePost(request)).thenReturn(analysis);
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> {
            Post saved = inv.getArgument(0);
            assertThat(saved.isAiFlagged()).isFalse();
            return post;
        });
        when(postLikeRepository.countByPostId(any())).thenReturn(0L);

        postService.createPost(request, author);
    }

    @Test
    void createPost_savesAiReasoningToPost() {
        CreatePostRequest request = new CreatePostRequest("Title", "Body");
        PostAnalysisResponse analysis = new PostAnalysisResponse(0.3, "Slight concern");

        when(moderatorService.analysePost(request)).thenReturn(analysis);
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> {
            Post saved = inv.getArgument(0);
            assertThat(saved.getAiReasoning()).isEqualTo("Slight concern");
            return post;
        });
        when(postLikeRepository.countByPostId(any())).thenReturn(0L);

        postService.createPost(request, author);
    }

    // --- GET ALL POSTS ---

    @Test
    void getAllPosts_returnsMappedList() {
        when(postRepository.findAllByOrderByUpdatedAtDesc()).thenReturn(List.of(post));
        when(postLikeRepository.countByPostId(postId)).thenReturn(5L);

        List<PostResponse> result = postService.getAllPosts(otherUser);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Test Title");
        assertThat(result.get(0).likeCount()).isEqualTo(5L);
    }

    @Test
    void getAllPosts_emptyList_returnsEmpty() {
        when(postRepository.findAllByOrderByUpdatedAtDesc()).thenReturn(List.of());

        List<PostResponse> result = postService.getAllPosts(otherUser);

        assertThat(result).isEmpty();
    }

    // --- GET POST BY ID ---

    @Test
    void getPostById_found_returnsResponse() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postLikeRepository.countByPostId(postId)).thenReturn(0L);

        PostResponse result = postService.getPostById(postId, otherUser);

        assertThat(result.id()).isEqualTo(postId);
        assertThat(result.title()).isEqualTo("Test Title");
    }

    @Test
    void getPostById_notFound_throwsException() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPostById(postId, otherUser))
                .isInstanceOf(PostNotFoundException.class);
    }

    // --- GET ALL FLAGGED POSTS ---

    @Test
    void getAllFlaggedPosts_returnsFlaggedOnly() {
        Post flaggedPost = Post.builder()
                .id(UUID.randomUUID())
                .title("Flagged")
                .body("Bad content")
                .postAuthor(author)
                .aiFlagged(true)
                .aiScore(0.9)
                .aiReasoning("Toxic")
                .updatedAt(Instant.now())
                .build();

        when(postRepository.findByAiFlaggedTrue()).thenReturn(List.of(flaggedPost));
        when(postLikeRepository.countByPostId(any())).thenReturn(0L);

        List<PostResponse> result = postService.getAllFlaggedPosts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).aiFlagged()).isTrue();
    }

    // --- LIKE POST ---

    @Test
    void likePost_success_savesLike() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postLikeRepository.existsByPostIdAndUserId(postId, otherUser.getId())).thenReturn(false);

        postService.likePost(postId, otherUser);

        verify(postLikeRepository).save(any(PostLike.class));
    }

    @Test
    void likePost_ownPost_throwsConflict() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.likePost(postId, author))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("not allowed to like your own post");
    }

    @Test
    void likePost_alreadyLiked_throwsConflict() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postLikeRepository.existsByPostIdAndUserId(postId, otherUser.getId())).thenReturn(true);

        assertThatThrownBy(() -> postService.likePost(postId, otherUser))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already liked");
    }

    @Test
    void likePost_postNotFound_throwsException() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.likePost(postId, otherUser))
                .isInstanceOf(PostNotFoundException.class);
    }

    // --- UNLIKE POST ---

    @Test
    void unlikePost_success_deletesLike() {
        PostLike like = PostLike.builder()
                .id(UUID.randomUUID())
                .post(post)
                .user(otherUser)
                .build();

        when(postLikeRepository.findByPostIdAndUserId(postId, otherUser.getId()))
                .thenReturn(Optional.of(like));

        postService.unlikePost(postId, otherUser);

        verify(postLikeRepository).delete(like);
    }

    @Test
    void unlikePost_notLiked_throwsConflict() {
        when(postLikeRepository.findByPostIdAndUserId(postId, otherUser.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.unlikePost(postId, otherUser))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("have not liked this post");
    }

    // --- FLAG POST ---

    @Test
    void flagPost_success_setsFlaggedMisleadingTrue() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        postService.flagPost(postId);

        assertThat(post.isFlaggedMisleading()).isTrue();
        verify(postRepository).save(post);
    }

    @Test
    void flagPost_postNotFound_throwsException() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.flagPost(postId))
                .isInstanceOf(PostNotFoundException.class);
    }

    // --- UNFLAG POST ---

    @Test
    void unflagPost_success_setsFlaggedMisleadingFalse() {
        post.setFlaggedMisleading(true);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        postService.unflagPost(postId);

        assertThat(post.isFlaggedMisleading()).isFalse();
        verify(postRepository).save(post);
    }

    @Test
    void unflagPost_postNotFound_throwsException() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.unflagPost(postId))
                .isInstanceOf(PostNotFoundException.class);
    }

    // --- ADD COMMENT ---

    @Test
    void addComment_success_returnsCommentResponse() {
        CreateCommentRequest request = new CreateCommentRequest("Great post!");

        Comment comment = Comment.builder()
                .id(UUID.randomUUID())
                .post(post)
                .author(otherUser)
                .body("Great post!")
                .build();

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentResponse response = postService.addComment(postId, request, otherUser);

        assertThat(response.body()).isEqualTo("Great post!");
        assertThat(response.authorUsername()).isEqualTo("other");
    }

    @Test
    void addComment_postNotFound_throwsException() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.addComment(postId, new CreateCommentRequest("hi"), otherUser))
                .isInstanceOf(PostNotFoundException.class);
    }

    // --- MAP TO RESPONSE ---

    @Test
    void mapToResponse_includesAiReasoning() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postLikeRepository.countByPostId(postId)).thenReturn(0L);

        PostResponse response = postService.getPostById(postId, otherUser);

        assertThat(response.aiReasoning()).isEqualTo("Clean post");
    }

    @Test
    void mapToResponse_includesCorrectLikeCount() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postLikeRepository.countByPostId(postId)).thenReturn(42L);

        PostResponse response = postService.getPostById(postId, otherUser);

        assertThat(response.likeCount()).isEqualTo(42L);
    }
}
