package com.cerex.repository;

import com.cerex.domain.User;
import com.cerex.domain.User.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link User} entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Optional<User> findByOauthProviderAndOauthProviderId(String provider, String providerId);

    @Query("""
        SELECT u FROM User u
        WHERE u.status = :status
        ORDER BY u.createdAt DESC
        """)
    Page<User> findByStatus(@Param("status") UserStatus status, Pageable pageable);

    /**
     * Find users pending deletion whose grace period has expired.
     */
    @Query("""
        SELECT u FROM User u
        WHERE u.status = 'PENDING_DELETION'
          AND u.dataDeletionRequestedAt < :cutoffDate
        """)
    List<User> findUsersReadyForDeletion(@Param("cutoffDate") Instant cutoffDate);

    /**
     * Count users registered after a given date.
     */
    long countByCreatedAtAfter(Instant since);
}
