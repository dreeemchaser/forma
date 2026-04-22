package com.forma.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table( name = "posts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue( strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn( name = "user_id", nullable = false)
    private User postAuthor;

    @Column( nullable = false, length = 255)
    private String title;

    @Column( nullable = false, length = 255)
    private String body;

    // AI Fields:
    private boolean aiFlagged = false;
    private double aiScore;

    // Moderation Fields:
    private boolean flaggedMisleading = false;
    @UpdateTimestamp
    private Instant updatedAt;

}
