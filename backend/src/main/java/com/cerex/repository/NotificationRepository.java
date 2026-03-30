package com.cerex.repository;

import com.cerex.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for Notification operations.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.recipientId = :userId AND n.isRead = false ORDER BY n.createdAt DESC")
    Page<Notification> findUnreadByRecipient(@Param("userId") UUID userId, Pageable pageable);

    long countByRecipientIdAndIsReadFalse(UUID recipientId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.recipientId = :userId AND n.isRead = false")
    void markAllAsRead(@Param("userId") UUID userId);
}
