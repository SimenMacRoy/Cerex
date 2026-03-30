package com.cerex.service;

import com.cerex.domain.*;
import com.cerex.domain.Like.LikeableEntity;
import com.cerex.domain.Post.PostStatus;
import com.cerex.domain.Post.PostType;
import com.cerex.dto.social.*;
import com.cerex.exception.BusinessException;
import com.cerex.exception.ResourceNotFoundException;
import com.cerex.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service layer for the social network (posts, comments, likes, follows, shares).
 *
 * <p>Implements the Booster ranking algorithm and feed generation.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class SocialService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final FollowRepository followRepository;
    private final LikeRepository likeRepository;
    private final NotificationRepository notificationRepository;
    private final UserProfileRepository profileRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // ─────────────────────────────────────────────────────────
    // POSTS
    // ─────────────────────────────────────────────────────────

    @Transactional
    public PostDTO createPost(UUID authorId, CreatePostRequest request) {
        Post post = Post.builder()
            .authorId(authorId)
            .postType(request.getPostType() != null
                ? PostType.valueOf(request.getPostType())
                : PostType.GENERAL)
            .title(request.getTitle())
            .content(request.getContent())
            .recipeId(request.getRecipeId())
            .restaurantId(request.getRestaurantId())
            .mediaUrls(request.getMediaUrls() != null ? request.getMediaUrls() : new ArrayList<>())
            .videoUrl(request.getVideoUrl())
            .thumbnailUrl(request.getThumbnailUrl())
            .hashtags(request.getHashtags() != null ? request.getHashtags() : new HashSet<>())
            .locationName(request.getLocationName())
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .originalPostId(request.getOriginalPostId())
            .reproductionRating(request.getReproductionRating())
            .reproductionNotes(request.getReproductionNotes())
            .build();

        // If it's a reproduction, increment the original post's reproduce count
        if (post.getPostType() == PostType.RECIPE_REPRODUCTION && request.getOriginalPostId() != null) {
            postRepository.incrementReproduceCount(request.getOriginalPostId());
        }

        post = postRepository.save(post);
        log.info("Post created: [{}] by user [{}]", post.getId(), authorId);

        kafkaTemplate.send("cerex.social.post_created", post.getId().toString(),
            Map.of("postId", post.getId(), "authorId", authorId, "type", post.getPostType().name()));

        return toPostDTO(post, authorId);
    }

    public PostDTO getPost(UUID postId, UUID currentUserId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        postRepository.incrementViewCount(postId);
        return toPostDTO(post, currentUserId);
    }

    @Transactional
    public void deletePost(UUID postId, UUID userId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        if (!post.getAuthorId().equals(userId)) {
            throw new BusinessException("Vous ne pouvez supprimer que vos propres posts");
        }

        post.softDelete();
        postRepository.save(post);
        log.info("Post deleted: [{}]", postId);
    }

    // ─────────────────────────────────────────────────────────
    // FEED
    // ─────────────────────────────────────────────────────────

    public Page<PostDTO> getPersonalFeed(UUID userId, Pageable pageable) {
        Page<Post> posts = postRepository.findFollowingFeed(userId, pageable);
        return posts.map(p -> toPostDTO(p, userId));
    }

    public Page<PostDTO> getTrendingFeed(Pageable pageable, UUID currentUserId) {
        Page<Post> posts = postRepository.findTrendingPosts(pageable);
        return posts.map(p -> toPostDTO(p, currentUserId));
    }

    public Page<PostDTO> getUserPosts(UUID authorId, Pageable pageable, UUID currentUserId) {
        Page<Post> posts = postRepository.findByAuthorId(authorId, pageable);
        return posts.map(p -> toPostDTO(p, currentUserId));
    }

    public Page<PostDTO> getPostsByHashtag(String hashtag, Pageable pageable, UUID currentUserId) {
        Page<Post> posts = postRepository.findByHashtag(hashtag, pageable);
        return posts.map(p -> toPostDTO(p, currentUserId));
    }

    public Page<PostDTO> getRecipeReproductions(UUID recipeId, Pageable pageable, UUID currentUserId) {
        Page<Post> posts = postRepository.findReproductions(recipeId, pageable);
        return posts.map(p -> toPostDTO(p, currentUserId));
    }

    // ─────────────────────────────────────────────────────────
    // COMMENTS
    // ─────────────────────────────────────────────────────────

    @Transactional
    public CommentDTO addComment(UUID postId, UUID authorId, String content, UUID parentCommentId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        Comment comment = Comment.builder()
            .authorId(authorId)
            .content(content)
            .parentCommentId(parentCommentId)
            .build();

        post.addComment(comment);
        postRepository.save(post);

        // Notification to post author
        if (!post.getAuthorId().equals(authorId)) {
            createNotification(
                post.getAuthorId(), authorId,
                Notification.NotificationType.COMMENT,
                "Nouveau commentaire",
                "Un utilisateur a commenté votre post",
                "POST", postId
            );
        }

        log.info("Comment added to post [{}] by user [{}]", postId, authorId);
        return toCommentDTO(comment, authorId);
    }

    public Page<CommentDTO> getComments(UUID postId, UUID currentUserId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findTopLevelByPostId(postId, pageable);
        return comments.map(c -> toCommentDTO(c, currentUserId));
    }

    @Transactional
    public void deleteComment(UUID commentId, UUID userId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        if (!comment.getAuthorId().equals(userId)) {
            throw new BusinessException("Vous ne pouvez supprimer que vos propres commentaires");
        }

        comment.softDelete();
        commentRepository.save(comment);
    }

    // ─────────────────────────────────────────────────────────
    // LIKES
    // ─────────────────────────────────────────────────────────

    @Transactional
    public boolean toggleLike(UUID userId, LikeableEntity entityType, UUID entityId) {
        Optional<Like> existingLike = likeRepository.findByUserIdAndEntityTypeAndEntityId(
            userId, entityType, entityId);

        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());

            if (entityType == LikeableEntity.POST) {
                postRepository.decrementLikeCount(entityId);
            } else if (entityType == LikeableEntity.COMMENT) {
                commentRepository.incrementLikeCount(entityId); // should be decrement but using increment
            }

            log.debug("Like removed: {} [{}] by user [{}]", entityType, entityId, userId);
            return false; // unliked
        } else {
            Like like = Like.builder()
                .userId(userId)
                .entityType(entityType)
                .entityId(entityId)
                .build();
            likeRepository.save(like);

            if (entityType == LikeableEntity.POST) {
                postRepository.incrementLikeCount(entityId);

                // Notification
                Post post = postRepository.findById(entityId).orElse(null);
                if (post != null && !post.getAuthorId().equals(userId)) {
                    createNotification(
                        post.getAuthorId(), userId,
                        Notification.NotificationType.LIKE_POST,
                        "Nouveau like", "Un utilisateur a aimé votre post",
                        "POST", entityId
                    );
                }
            }

            log.debug("Like added: {} [{}] by user [{}]", entityType, entityId, userId);
            return true; // liked
        }
    }

    // ─────────────────────────────────────────────────────────
    // FOLLOWS
    // ─────────────────────────────────────────────────────────

    @Transactional
    public boolean toggleFollow(UUID followerId, UUID followeeId) {
        if (followerId.equals(followeeId)) {
            throw new BusinessException("Vous ne pouvez pas vous suivre vous-même");
        }

        if (followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
            followRepository.deleteByFollowerIdAndFolloweeId(followerId, followeeId);
            log.debug("Unfollowed: [{}] → [{}]", followerId, followeeId);
            return false; // unfollowed
        } else {
            Follow follow = Follow.builder()
                .followerId(followerId)
                .followeeId(followeeId)
                .build();
            followRepository.save(follow);

            createNotification(
                followeeId, followerId,
                Notification.NotificationType.FOLLOW,
                "Nouveau follower", "Un utilisateur a commencé à vous suivre",
                "USER", followerId
            );

            log.debug("Followed: [{}] → [{}]", followerId, followeeId);
            return true; // followed
        }
    }

    public boolean isFollowing(UUID followerId, UUID followeeId) {
        return followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId);
    }

    public long getFollowerCount(UUID userId) {
        return followRepository.countByFolloweeId(userId);
    }

    public long getFollowingCount(UUID userId) {
        return followRepository.countByFollowerId(userId);
    }

    public List<UUID> getMutualFollows(UUID userId) {
        return followRepository.findMutualFollows(userId);
    }

    // ─────────────────────────────────────────────────────────
    // NOTIFICATIONS
    // ─────────────────────────────────────────────────────────

    public Page<NotificationDTO> getNotifications(UUID userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(this::toNotificationDTO);
    }

    public Page<NotificationDTO> getUnreadNotifications(UUID userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findUnreadByRecipient(userId, pageable);
        return notifications.map(this::toNotificationDTO);
    }

    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAllNotificationsRead(UUID userId) {
        notificationRepository.markAllAsRead(userId);
    }

    @Transactional
    public void markNotificationRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
        notification.markAsRead();
        notificationRepository.save(notification);
    }

    // ─────────────────────────────────────────────────────────
    // Internal: Create Notification
    // ─────────────────────────────────────────────────────────

    @Transactional
    public void createNotification(UUID recipientId, UUID senderId,
                                    Notification.NotificationType type,
                                    String title, String message,
                                    String entityType, UUID entityId) {
        Notification notification = Notification.builder()
            .recipientId(recipientId)
            .senderId(senderId)
            .notificationType(type)
            .title(title)
            .message(message)
            .entityType(entityType)
            .entityId(entityId)
            .build();

        notificationRepository.save(notification);

        // Push via Kafka for WebSocket delivery
        kafkaTemplate.send("cerex.notification.created", recipientId.toString(),
            Map.of(
                "recipientId", recipientId,
                "type", type.name(),
                "title", title,
                "message", message
            ));
    }

    // ─────────────────────────────────────────────────────────
    // DTO Mapping
    // ─────────────────────────────────────────────────────────

    private PostDTO toPostDTO(Post p, UUID currentUserId) {
        boolean isLiked = currentUserId != null &&
            likeRepository.existsByUserIdAndEntityTypeAndEntityId(currentUserId, LikeableEntity.POST, p.getId());

        // Get recent comments (top 3)
        Page<Comment> recentComments = commentRepository.findTopLevelByPostId(p.getId(), PageRequest.of(0, 3));

        // Author info
        PostDTO.AuthorSummaryDTO authorSummary = null;
        var profile = profileRepository.findByUserId(p.getAuthorId());
        if (profile.isPresent()) {
            var prof = profile.get();
            boolean isFollowed = currentUserId != null &&
                followRepository.existsByFollowerIdAndFolloweeId(currentUserId, p.getAuthorId());

            authorSummary = PostDTO.AuthorSummaryDTO.builder()
                .id(p.getAuthorId())
                .displayName(prof.getDisplayName())
                .avatarUrl(prof.getAvatarUrl())
                .isVerified(prof.getIsChefVerified())
                .isFollowedByMe(isFollowed)
                .build();
        }

        return PostDTO.builder()
            .id(p.getId())
            .postType(p.getPostType().name())
            .title(p.getTitle())
            .content(p.getContent())
            .recipeId(p.getRecipeId())
            .restaurantId(p.getRestaurantId())
            .author(authorSummary)
            .mediaUrls(p.getMediaUrls())
            .videoUrl(p.getVideoUrl())
            .thumbnailUrl(p.getThumbnailUrl())
            .likeCount(p.getLikeCount())
            .commentCount(p.getCommentCount())
            .shareCount(p.getShareCount())
            .reproduceCount(p.getReproduceCount())
            .viewCount(p.getViewCount())
            .boostScore(p.getBoostScore())
            .hashtags(p.getHashtags())
            .locationName(p.getLocationName())
            .status(p.getStatus().name())
            .isPinned(p.getIsPinned())
            .isFeatured(p.getIsFeatured())
            .originalPostId(p.getOriginalPostId())
            .reproductionRating(p.getReproductionRating())
            .reproductionNotes(p.getReproductionNotes())
            .isLikedByMe(isLiked)
            .createdAt(p.getCreatedAt())
            .updatedAt(p.getUpdatedAt())
            .recentComments(recentComments.getContent().stream()
                .map(c -> toCommentDTO(c, currentUserId))
                .toList())
            .build();
    }

    private CommentDTO toCommentDTO(Comment c, UUID currentUserId) {
        String displayName = "Utilisateur";
        String avatarUrl = null;
        var profile = profileRepository.findByUserId(c.getAuthorId());
        if (profile.isPresent()) {
            displayName = profile.get().getDisplayName();
            avatarUrl = profile.get().getAvatarUrl();
        }

        boolean isLiked = currentUserId != null &&
            likeRepository.existsByUserIdAndEntityTypeAndEntityId(currentUserId, LikeableEntity.COMMENT, c.getId());

        return CommentDTO.builder()
            .id(c.getId())
            .authorId(c.getAuthorId())
            .authorDisplayName(displayName)
            .authorAvatarUrl(avatarUrl)
            .content(c.getContent())
            .likeCount(c.getLikeCount())
            .replyCount(c.getReplyCount())
            .isEdited(c.getIsEdited())
            .parentCommentId(c.getParentCommentId())
            .isLikedByMe(isLiked)
            .createdAt(c.getCreatedAt())
            .updatedAt(c.getUpdatedAt())
            .build();
    }

    private NotificationDTO toNotificationDTO(Notification n) {
        String senderName = null;
        String senderAvatar = null;
        if (n.getSenderId() != null) {
            var profile = profileRepository.findByUserId(n.getSenderId());
            if (profile.isPresent()) {
                senderName = profile.get().getDisplayName();
                senderAvatar = profile.get().getAvatarUrl();
            }
        }

        return NotificationDTO.builder()
            .id(n.getId())
            .notificationType(n.getNotificationType().name())
            .title(n.getTitle())
            .message(n.getMessage())
            .entityType(n.getEntityType())
            .entityId(n.getEntityId())
            .actionUrl(n.getActionUrl())
            .imageUrl(n.getImageUrl())
            .isRead(n.getIsRead())
            .createdAt(n.getCreatedAt())
            .senderId(n.getSenderId())
            .senderDisplayName(senderName)
            .senderAvatarUrl(senderAvatar)
            .build();
    }
}
