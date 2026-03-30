package com.cerex.controller;

import com.cerex.domain.Like.LikeableEntity;
import com.cerex.dto.ApiResponse;
import com.cerex.dto.social.*;
import com.cerex.security.CerexUserDetails;
import com.cerex.service.SocialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for the social network.
 *
 * <p>Endpoints for posts, comments, likes, follows, feed, and notifications.
 */
@RestController
@RequestMapping("/api/v1/social")
@RequiredArgsConstructor
@Tag(name = "Social Network", description = "Réseau social culinaire APIs")
public class SocialController {

    private final SocialService socialService;

    // ─────────────────────────────────────────────────────────
    // POSTS
    // ─────────────────────────────────────────────────────────

    @PostMapping("/posts")
    @Operation(summary = "Créer un post")
    public ResponseEntity<ApiResponse<PostDTO>> createPost(
            @AuthenticationPrincipal CerexUserDetails user,
            @Valid @RequestBody CreatePostRequest request) {
        PostDTO dto = socialService.createPost(user.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(dto));
    }

    @GetMapping("/posts/{postId}")
    @Operation(summary = "Détails d'un post")
    public ResponseEntity<ApiResponse<PostDTO>> getPost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal CerexUserDetails user) {
        UUID currentUserId = user != null ? user.getUserId() : null;
        return ResponseEntity.ok(ApiResponse.success(
            socialService.getPost(postId, currentUserId)));
    }

    @DeleteMapping("/posts/{postId}")
    @Operation(summary = "Supprimer un post")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal CerexUserDetails user) {
        socialService.deletePost(postId, user.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "Post supprimé"));
    }

    // ─────────────────────────────────────────────────────────
    // FEED
    // ─────────────────────────────────────────────────────────

    @GetMapping("/feed")
    @Operation(summary = "Mon fil d'actualités (posts des personnes suivies)")
    public ResponseEntity<ApiResponse<Page<PostDTO>>> getPersonalFeed(
            @AuthenticationPrincipal CerexUserDetails user,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
            socialService.getPersonalFeed(user.getUserId(), pageable)));
    }

    @GetMapping("/feed/trending")
    @Operation(summary = "Posts tendances (explore)")
    public ResponseEntity<ApiResponse<Page<PostDTO>>> getTrendingFeed(
            @AuthenticationPrincipal CerexUserDetails user,
            @PageableDefault(size = 20) Pageable pageable) {
        UUID currentUserId = user != null ? user.getUserId() : null;
        return ResponseEntity.ok(ApiResponse.success(
            socialService.getTrendingFeed(pageable, currentUserId)));
    }

    @GetMapping("/users/{userId}/posts")
    @Operation(summary = "Posts d'un utilisateur")
    public ResponseEntity<ApiResponse<Page<PostDTO>>> getUserPosts(
            @PathVariable UUID userId,
            @AuthenticationPrincipal CerexUserDetails user,
            @PageableDefault(size = 20) Pageable pageable) {
        UUID currentUserId = user != null ? user.getUserId() : null;
        return ResponseEntity.ok(ApiResponse.success(
            socialService.getUserPosts(userId, pageable, currentUserId)));
    }

    @GetMapping("/hashtags/{hashtag}")
    @Operation(summary = "Posts par hashtag")
    public ResponseEntity<ApiResponse<Page<PostDTO>>> getPostsByHashtag(
            @PathVariable String hashtag,
            @AuthenticationPrincipal CerexUserDetails user,
            @PageableDefault(size = 20) Pageable pageable) {
        UUID currentUserId = user != null ? user.getUserId() : null;
        return ResponseEntity.ok(ApiResponse.success(
            socialService.getPostsByHashtag(hashtag, pageable, currentUserId)));
    }

    @GetMapping("/recipes/{recipeId}/reproductions")
    @Operation(summary = "Reproductions d'une recette")
    public ResponseEntity<ApiResponse<Page<PostDTO>>> getReproductions(
            @PathVariable UUID recipeId,
            @AuthenticationPrincipal CerexUserDetails user,
            @PageableDefault(size = 20) Pageable pageable) {
        UUID currentUserId = user != null ? user.getUserId() : null;
        return ResponseEntity.ok(ApiResponse.success(
            socialService.getRecipeReproductions(recipeId, pageable, currentUserId)));
    }

    // ─────────────────────────────────────────────────────────
    // COMMENTS
    // ─────────────────────────────────────────────────────────

    @PostMapping("/posts/{postId}/comments")
    @Operation(summary = "Ajouter un commentaire")
    public ResponseEntity<ApiResponse<CommentDTO>> addComment(
            @PathVariable UUID postId,
            @AuthenticationPrincipal CerexUserDetails user,
            @RequestParam String content,
            @RequestParam(required = false) UUID parentCommentId) {
        CommentDTO dto = socialService.addComment(postId, user.getUserId(), content, parentCommentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(dto));
    }

    @GetMapping("/posts/{postId}/comments")
    @Operation(summary = "Commentaires d'un post")
    public ResponseEntity<ApiResponse<Page<CommentDTO>>> getComments(
            @PathVariable UUID postId,
            @AuthenticationPrincipal CerexUserDetails user,
            @PageableDefault(size = 20) Pageable pageable) {
        UUID currentUserId = user != null ? user.getUserId() : null;
        return ResponseEntity.ok(ApiResponse.success(
            socialService.getComments(postId, currentUserId, pageable)));
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "Supprimer un commentaire")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable UUID commentId,
            @AuthenticationPrincipal CerexUserDetails user) {
        socialService.deleteComment(commentId, user.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "Commentaire supprimé"));
    }

    // ─────────────────────────────────────────────────────────
    // LIKES
    // ─────────────────────────────────────────────────────────

    @PostMapping("/posts/{postId}/like")
    @Operation(summary = "Toggle like sur un post")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> togglePostLike(
            @PathVariable UUID postId,
            @AuthenticationPrincipal CerexUserDetails user) {
        boolean liked = socialService.toggleLike(user.getUserId(), LikeableEntity.POST, postId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("liked", liked)));
    }

    @PostMapping("/comments/{commentId}/like")
    @Operation(summary = "Toggle like sur un commentaire")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> toggleCommentLike(
            @PathVariable UUID commentId,
            @AuthenticationPrincipal CerexUserDetails user) {
        boolean liked = socialService.toggleLike(user.getUserId(), LikeableEntity.COMMENT, commentId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("liked", liked)));
    }

    // ─────────────────────────────────────────────────────────
    // FOLLOWS
    // ─────────────────────────────────────────────────────────

    @PostMapping("/users/{userId}/follow")
    @Operation(summary = "Toggle follow/unfollow un utilisateur")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> toggleFollow(
            @PathVariable UUID userId,
            @AuthenticationPrincipal CerexUserDetails user) {
        boolean following = socialService.toggleFollow(user.getUserId(), userId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("following", following)));
    }

    @GetMapping("/users/{userId}/followers/count")
    @Operation(summary = "Nombre de followers")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getFollowerCount(@PathVariable UUID userId) {
        long followers = socialService.getFollowerCount(userId);
        long following = socialService.getFollowingCount(userId);
        return ResponseEntity.ok(ApiResponse.success(
            Map.of("followers", followers, "following", following)));
    }

    @GetMapping("/users/{userId}/mutual-friends")
    @Operation(summary = "Amis en commun (follows mutuels)")
    public ResponseEntity<ApiResponse<List<UUID>>> getMutualFollows(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(
            socialService.getMutualFollows(userId)));
    }

    // ─────────────────────────────────────────────────────────
    // NOTIFICATIONS
    // ─────────────────────────────────────────────────────────

    @GetMapping("/notifications")
    @Operation(summary = "Mes notifications")
    public ResponseEntity<ApiResponse<Page<NotificationDTO>>> getNotifications(
            @AuthenticationPrincipal CerexUserDetails user,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
            socialService.getNotifications(user.getUserId(), pageable)));
    }

    @GetMapping("/notifications/unread")
    @Operation(summary = "Notifications non lues")
    public ResponseEntity<ApiResponse<Page<NotificationDTO>>> getUnreadNotifications(
            @AuthenticationPrincipal CerexUserDetails user,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
            socialService.getUnreadNotifications(user.getUserId(), pageable)));
    }

    @GetMapping("/notifications/unread/count")
    @Operation(summary = "Nombre de notifications non lues")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal CerexUserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(
            Map.of("count", socialService.getUnreadCount(user.getUserId()))));
    }

    @PatchMapping("/notifications/read-all")
    @Operation(summary = "Marquer toutes les notifications comme lues")
    public ResponseEntity<ApiResponse<Void>> markAllRead(
            @AuthenticationPrincipal CerexUserDetails user) {
        socialService.markAllNotificationsRead(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "Toutes les notifications marquées comme lues"));
    }

    @PatchMapping("/notifications/{notificationId}/read")
    @Operation(summary = "Marquer une notification comme lue")
    public ResponseEntity<ApiResponse<Void>> markRead(
            @PathVariable UUID notificationId) {
        socialService.markNotificationRead(notificationId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
