package com.cerex.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Gamification and sustainability achievement badges.
 *
 * <p>Badges are awarded for eco-friendly cooking, cultural exploration,
 * community contributions, and platform milestones.
 *
 * @author Cerex Engineering Team
 * @version 1.0.0
 */
@Entity
@Table(
    name = "eco_badges",
    schema = "users_schema",
    indexes = {
        @Index(name = "idx_eco_badges_category", columnList = "category"),
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EcoBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @Size(max = 500)
    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    /** Badge category: ECO, CULTURAL, SOCIAL, MILESTONE */
    @NotBlank
    @Column(name = "category", nullable = false, length = 30)
    private String category;

    /** Criteria description for earning this badge. */
    @Column(name = "criteria", columnDefinition = "TEXT")
    private String criteria;

    /** Points awarded when the badge is earned. */
    @Column(name = "points")
    @Builder.Default
    private Integer points = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
