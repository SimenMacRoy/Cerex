package com.cerex.repository;

import com.cerex.domain.Like;
import com.cerex.domain.Like.LikeableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Like operations.
 */
@Repository
public interface LikeRepository extends JpaRepository<Like, UUID> {

    Optional<Like> findByUserIdAndEntityTypeAndEntityId(UUID userId, LikeableEntity entityType, UUID entityId);

    boolean existsByUserIdAndEntityTypeAndEntityId(UUID userId, LikeableEntity entityType, UUID entityId);

    void deleteByUserIdAndEntityTypeAndEntityId(UUID userId, LikeableEntity entityType, UUID entityId);

    long countByEntityTypeAndEntityId(LikeableEntity entityType, UUID entityId);

    @Query("SELECT l.entityId FROM Like l WHERE l.userId = :userId AND l.entityType = :entityType")
    List<UUID> findLikedEntityIds(@Param("userId") UUID userId, @Param("entityType") LikeableEntity entityType);
}
