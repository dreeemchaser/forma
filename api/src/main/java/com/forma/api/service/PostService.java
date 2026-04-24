package com.forma.api.service;

import com.forma.api.dto.*;
import com.forma.api.model.Comment;
import com.forma.api.model.Post;
import com.forma.api.model.PostLike;
import com.forma.api.model.User;
import com.forma.api.repository.CommentRepository;
import com.forma.api.repository.PostLikeRepository;
import com.forma.api.repository.PostRepository;
import com.forma.api.utils.exceptions.ConflictException;
import com.forma.api.utils.exceptions.PostNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final PostAnalysisInterface moderatorService;

    @Transactional
    public PostResponse createPost(CreatePostRequest request, User author) {

        // Analyze the content - request
        PostAnalysisResponse postAnalysisResponse =  moderatorService.analysePost(request);
        boolean aiFlagged = postAnalysisResponse.score() > 0.6;

        Post post = postRepository.save(
                Post.builder()
                .postAuthor(author)
                .title(request.title())
                .body(request.body())
                .aiFlagged(aiFlagged)
                .aiScore(postAnalysisResponse.score())
                .aiReasoning(postAnalysisResponse.reasoning())
                .build());

        return mapToResponse(post);
    }

    public List<PostResponse> getAllPosts() {
        return postRepository.findAllByOrderByUpdatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<PostResponse> getAllFlaggedPosts() {
        return postRepository.findByAiFlaggedTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public PostResponse getPostById(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id.toString()));
        return mapToResponse(post);
    }

    @Transactional
    public void unlikePost(UUID postId, User user) {
        PostLike like = postLikeRepository.findByPostIdAndUserId(postId, user.getId())
                .orElseThrow(() -> new ConflictException("You have not liked this post"));
        postLikeRepository.delete(like);
    }

    @Transactional
    public void likePost(UUID postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId.toString()));

        if (post.getPostAuthor().getId().equals(user.getId())) {
            throw new ConflictException("You are not allowed to like your own post");
        }

        if (postLikeRepository.existsByPostIdAndUserId(postId, user.getId())) {
            throw new ConflictException("You have already liked this post");
        }

        postLikeRepository.save(PostLike.builder().post(post).user(user).build());
    }

    @Transactional
    public void flagPost(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId.toString()));
        post.setFlaggedMisleading(true);
        postRepository.save(post);
    }

    @Transactional
    public void unflagPost(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId.toString()));
        post.setFlaggedMisleading(false);
        postRepository.save(post);
    }

    @Transactional
    public CommentResponse addComment(UUID postId, CreateCommentRequest request, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId.toString()));

        Comment comment = commentRepository.save(Comment.builder()
                .post(post)
                .author(user)
                .body(request.body())
                .build());

        return new CommentResponse(
                comment.getId(),
                comment.getAuthor().getUsername(),
                comment.getBody(),
                comment.getCreatedAt()
        );
    }

    private PostResponse mapToResponse(Post post) {
        long likeCount = postLikeRepository.countByPostId(post.getId());
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .body(post.getBody())
                .authorUsername(post.getPostAuthor().getUsername())
                .likeCount(likeCount)
                .aiFlagged(post.isAiFlagged())
                .aiReasoning(post.getAiReasoning())
                .flaggedMisleading(post.isFlaggedMisleading())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

}
