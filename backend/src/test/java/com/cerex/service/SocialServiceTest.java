package com.cerex.service;

import com.cerex.domain.*;
import com.cerex.domain.Like.LikeableEntity;
import com.cerex.domain.Post.PostType;
import com.cerex.dto.social.CreatePostRequest;
import com.cerex.dto.social.PostDTO;
import com.cerex.exception.BusinessException;
import com.cerex.exception.ResourceNotFoundException;
import com.cerex.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for SocialService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SocialService Tests")
class SocialServiceTest {

    @Mock private PostRepository postRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private FollowRepository followRepository;
    @Mock private LikeRepository likeRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private UserProfileRepository profileRepository;
    @Mock private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private SocialService socialService;

    private UUID userId;
    private UUID otherUserId;
    private Post testPost;
    private UserProfile testProfile;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();

        testPost = Post.builder()
            .id(UUID.randomUUID())
            .authorId(userId)
            .postType(PostType.FOOD_PHOTO)
            .content("Mon plat du jour ! 🍝")
            .status(Post.PostStatus.ACTIVE)
            .likeCount(5)
            .commentCount(2)
            .boostScore(15.0)
            .createdAt(Instant.now())
            .hashtags(Set.of("cuisine", "italie"))
            .build();

        testProfile = UserProfile.builder()
            .id(UUID.randomUUID())
            .userId(userId)
            .displayName("Chef Jean")
            .avatarUrl("https://cdn.cerex.com/avatar.jpg")
            .isChefVerified(true)
            .build();
    }

    @Nested
    @DisplayName("Posts")
    class PostTests {

        @Test
        @DisplayName("Should create a post successfully")
        void shouldCreatePost() {
            // Given
            CreatePostRequest request = CreatePostRequest.builder()
                .content("Découverte du jour ! 🌍")
                .postType("FOOD_PHOTO")
                .hashtags(Set.of("discover", "food"))
                .build();

            given(postRepository.save(any(Post.class))).willAnswer(inv -> {
                Post p = inv.getArgument(0);
                p.setId(UUID.randomUUID());
                p.setCreatedAt(Instant.now());
                return p;
            });
            given(profileRepository.findByUserId(userId)).willReturn(Optional.of(testProfile));
            given(followRepository.existsByFollowerIdAndFolloweeId(any(), any())).willReturn(false);
            given(likeRepository.existsByUserIdAndEntityTypeAndEntityId(any(), any(), any())).willReturn(false);
            given(commentRepository.findTopLevelByPostId(any(), any())).willReturn(Page.empty());
            given(kafkaTemplate.send(anyString(), anyString(), any()))
                .willReturn(CompletableFuture.completedFuture(null));

            // When
            PostDTO result = socialService.createPost(userId, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEqualTo("Découverte du jour ! 🌍");
            then(postRepository).should().save(any(Post.class));
        }

        @Test
        @DisplayName("Should delete own post")
        void shouldDeleteOwnPost() {
            // Given
            given(postRepository.findById(testPost.getId())).willReturn(Optional.of(testPost));
            given(postRepository.save(any(Post.class))).willAnswer(inv -> inv.getArgument(0));

            // When
            socialService.deletePost(testPost.getId(), userId);

            // Then
            assertThat(testPost.getStatus()).isEqualTo(Post.PostStatus.DELETED);
        }

        @Test
        @DisplayName("Should not delete another user's post")
        void shouldNotDeleteOtherUserPost() {
            // Given
            given(postRepository.findById(testPost.getId())).willReturn(Optional.of(testPost));

            // When & Then
            assertThatThrownBy(() -> socialService.deletePost(testPost.getId(), otherUserId))
                .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("Likes")
    class LikeTests {

        @Test
        @DisplayName("Should toggle like on - first like")
        void shouldToggleLikeOn() {
            // Given
            given(likeRepository.findByUserIdAndEntityTypeAndEntityId(userId, LikeableEntity.POST, testPost.getId()))
                .willReturn(Optional.empty());
            given(likeRepository.save(any(Like.class))).willAnswer(inv -> inv.getArgument(0));
            given(postRepository.findById(testPost.getId())).willReturn(Optional.of(testPost));
            given(notificationRepository.save(any(Notification.class))).willAnswer(inv -> inv.getArgument(0));
            given(kafkaTemplate.send(anyString(), anyString(), any()))
                .willReturn(CompletableFuture.completedFuture(null));

            // When
            boolean liked = socialService.toggleLike(userId, LikeableEntity.POST, testPost.getId());

            // Then
            assertThat(liked).isTrue();
            then(likeRepository).should().save(any(Like.class));
            then(postRepository).should().incrementLikeCount(testPost.getId());
        }

        @Test
        @DisplayName("Should toggle like off - unlike")
        void shouldToggleLikeOff() {
            // Given
            Like existingLike = Like.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .entityType(LikeableEntity.POST)
                .entityId(testPost.getId())
                .build();

            given(likeRepository.findByUserIdAndEntityTypeAndEntityId(userId, LikeableEntity.POST, testPost.getId()))
                .willReturn(Optional.of(existingLike));

            // When
            boolean liked = socialService.toggleLike(userId, LikeableEntity.POST, testPost.getId());

            // Then
            assertThat(liked).isFalse();
            then(likeRepository).should().delete(existingLike);
            then(postRepository).should().decrementLikeCount(testPost.getId());
        }
    }

    @Nested
    @DisplayName("Follows")
    class FollowTests {

        @Test
        @DisplayName("Should follow another user")
        void shouldFollowUser() {
            // Given
            given(followRepository.existsByFollowerIdAndFolloweeId(userId, otherUserId)).willReturn(false);
            given(followRepository.save(any(Follow.class))).willAnswer(inv -> inv.getArgument(0));
            given(notificationRepository.save(any(Notification.class))).willAnswer(inv -> inv.getArgument(0));
            given(kafkaTemplate.send(anyString(), anyString(), any()))
                .willReturn(CompletableFuture.completedFuture(null));

            // When
            boolean following = socialService.toggleFollow(userId, otherUserId);

            // Then
            assertThat(following).isTrue();
            then(followRepository).should().save(any(Follow.class));
        }

        @Test
        @DisplayName("Should unfollow a user")
        void shouldUnfollowUser() {
            // Given
            given(followRepository.existsByFollowerIdAndFolloweeId(userId, otherUserId)).willReturn(true);

            // When
            boolean following = socialService.toggleFollow(userId, otherUserId);

            // Then
            assertThat(following).isFalse();
            then(followRepository).should().deleteByFollowerIdAndFolloweeId(userId, otherUserId);
        }

        @Test
        @DisplayName("Should not follow yourself")
        void shouldNotFollowSelf() {
            // When & Then
            assertThatThrownBy(() -> socialService.toggleFollow(userId, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("vous-même");
        }
    }

    @Nested
    @DisplayName("Feed")
    class FeedTests {

        @Test
        @DisplayName("Should get personal feed")
        void shouldGetPersonalFeed() {
            // Given
            Page<Post> posts = new PageImpl<>(List.of(testPost));
            given(postRepository.findFollowingFeed(userId, PageRequest.of(0, 20))).willReturn(posts);
            given(profileRepository.findByUserId(any())).willReturn(Optional.of(testProfile));
            given(likeRepository.existsByUserIdAndEntityTypeAndEntityId(any(), any(), any())).willReturn(false);
            given(followRepository.existsByFollowerIdAndFolloweeId(any(), any())).willReturn(false);
            given(commentRepository.findTopLevelByPostId(any(), any())).willReturn(Page.empty());

            // When
            Page<PostDTO> result = socialService.getPersonalFeed(userId, PageRequest.of(0, 20));

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getContent().get(0).getContent()).isEqualTo("Mon plat du jour ! 🍝");
        }
    }
}
