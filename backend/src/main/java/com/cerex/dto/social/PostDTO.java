package com.cerex.dto.social;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for social post responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {

    private UUID id;
    private String postType;
    private String title;
    private String content;
    private UUID recipeId;
    private UUID restaurantId;

    // Author
    private AuthorSummaryDTO author;

    // Media
    private List<String> mediaUrls;
    private String videoUrl;
    private String thumbnailUrl;

    // Engagement
    private Integer likeCount;
    private Integer commentCount;
    private Integer shareCount;
    private Integer reproduceCount;
    private Integer viewCount;
    private Double boostScore;

    // Tags & Location
    private Set<String> hashtags;
    private String locationName;

    // Status
    private String status;
    private Boolean isPinned;
    private Boolean isFeatured;

    // Reproduction
    private UUID originalPostId;
    private Integer reproductionRating;
    private String reproductionNotes;

    // User interaction state (for current user)
    private Boolean isLikedByMe;
    private Boolean isSavedByMe;

    // Timestamps
    private Instant createdAt;
    private Instant updatedAt;

    // Comments preview
    private List<CommentDTO> recentComments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorSummaryDTO {
        private UUID id;
        private String displayName;
        private String avatarUrl;
        private Boolean isVerified;
        private Boolean isFollowedByMe;
    }
}
