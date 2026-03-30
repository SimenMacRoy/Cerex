package com.cerex.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Media asset registry with processing status.
 *
 * <p>Stores metadata for images and videos uploaded to the platform.
 * Actual binary data lives in AWS S3; this table tracks URLs and processing state.
 *
 * @author Cerex Engineering Team
 * @version 1.0.0
 */
@Entity
@Table(
    name = "media",
    schema = "media_schema",
    indexes = {
        @Index(name = "idx_media_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_media_uploader", columnList = "uploaded_by"),
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    /** Type of parent entity: RECIPE, USER, RESTAURANT, POST */
    @NotBlank
    @Column(name = "entity_type", nullable = false, length = 30)
    private String entityType;

    /** ID of the parent entity this media belongs to. */
    @NotNull
    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    /** Media type: IMAGE, VIDEO, THUMBNAIL */
    @NotBlank
    @Column(name = "media_type", nullable = false, length = 20)
    private String mediaType;

    /** Original URL in S3. */
    @NotBlank
    @Size(max = 500)
    @Column(name = "original_url", nullable = false, length = 500)
    private String originalUrl;

    /** CDN-optimized URL (CloudFront). */
    @Size(max = 500)
    @Column(name = "cdn_url", length = 500)
    private String cdnUrl;

    /** Thumbnail URL for videos or large images. */
    @Size(max = 500)
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    /** MIME type (e.g., "image/webp", "video/mp4"). */
    @Size(max = 100)
    @Column(name = "mime_type", length = 100)
    private String mimeType;

    /** File size in bytes. */
    @Column(name = "file_size")
    private Long fileSize;

    /** Image/video width in pixels. */
    @Column(name = "width")
    private Integer width;

    /** Image/video height in pixels. */
    @Column(name = "height")
    private Integer height;

    /** Alt text for accessibility. */
    @Size(max = 300)
    @Column(name = "alt_text", length = 300)
    private String altText;

    /** Processing status: PENDING, PROCESSING, COMPLETED, FAILED */
    @Column(name = "processing_status", length = 20)
    @Builder.Default
    private String processingStatus = "PENDING";

    /** User who uploaded this media. */
    @Column(name = "uploaded_by")
    private UUID uploadedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
