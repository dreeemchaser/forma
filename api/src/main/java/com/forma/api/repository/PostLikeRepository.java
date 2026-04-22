package com.forma.api.repository;

import com.forma.api.model.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PostLikeRepository extends JpaRepository<PostLike, UUID> {
    Optional<PostLike> findByPostIdAndUserId(UUID postId, UUID userId);
    boolean existsByPostIdAndUserId(UUID postId, UUID userId);
    long countByPostId(UUID postId);
}
