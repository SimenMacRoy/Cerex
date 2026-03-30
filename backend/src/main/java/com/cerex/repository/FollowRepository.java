package com.cerex.repository;

import com.cerex.domain.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Follow (social graph) operations.
 */
@Repository
public interface FollowRepository extends JpaRepository<Follow, UUID> {

    Optional<Follow> findByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

    boolean existsByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

    void deleteByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

    /**
     * Get users I follow.
     */
    @Query("SELECT f.followeeId FROM Follow f WHERE f.followerId = :userId")
    List<UUID> findFolloweeIds(@Param("userId") UUID userId);

    /**
     * Get users who follow me.
     */
    @Query("SELECT f.followerId FROM Follow f WHERE f.followeeId = :userId")
    List<UUID> findFollowerIds(@Param("userId") UUID userId);

    long countByFollowerId(UUID followerId);  // following count
    long countByFolloweeId(UUID followeeId);  // followers count

    /**
     * Get followers with pagination.
     */
    @Query("SELECT f FROM Follow f WHERE f.followeeId = :userId ORDER BY f.createdAt DESC")
    Page<Follow> findFollowers(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Get following with pagination.
     */
    @Query("SELECT f FROM Follow f WHERE f.followerId = :userId ORDER BY f.createdAt DESC")
    Page<Follow> findFollowing(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Mutual follows (both follow each other).
     */
    @Query("""
        SELECT f1.followeeId FROM Follow f1
        WHERE f1.followerId = :userId
          AND f1.followeeId IN (
              SELECT f2.followerId FROM Follow f2 WHERE f2.followeeId = :userId
          )
        """)
    List<UUID> findMutualFollows(@Param("userId") UUID userId);
}
