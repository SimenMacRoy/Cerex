package com.cerex.dto.social;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO for comment responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {

    private UUID id;
    private UUID authorId;
    private String authorDisplayName;
    private String authorAvatarUrl;
    private String content;
    private Integer likeCount;
    private Integer replyCount;
    private Boolean isEdited;
    private UUID parentCommentId;
    private List<CommentDTO> replies;
    private Boolean isLikedByMe;
    private Instant createdAt;
    private Instant updatedAt;
}
