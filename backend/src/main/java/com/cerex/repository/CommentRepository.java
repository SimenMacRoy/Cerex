package com.cerex.repository;

import com.cerex.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Comment operations.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.parentCommentId IS NULL AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
    Page<Comment> findTopLevelByPostId(@Param("postId") UUID postId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.parentCommentId = :parentId AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findReplies(@Param("parentId") UUID parentCommentId);

    long countByPostIdAndDeletedAtIsNull(UUID postId);

    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :commentId")
    void incrementLikeCount(@Param("commentId") UUID commentId);
}
