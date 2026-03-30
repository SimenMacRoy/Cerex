package com.cerex.domain;

import 
jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Core domain entity for platform users.
 *
 * <p>Handles authentication identity, authorization roles, and account lifecycle.
 * Personal profile data is stored in {@link UserProfile} (1:1 relationship).
 *
 * <p>Supports:
 * <ul>
 *   <li>Email/password authentication with bcrypt hashing</li>
 *   <li>OAuth2 single sign-on (Google, Facebook, Apple)</li>
 *   <li>Multi-factor authentication (TOTP)</li>
 *   <li>Role-based access control (RBAC)</li>
 *   <li>GDPR soft deletion and anonymization</li>
 *   <li>Account lockout after repeated failed logins</li>
 * </ul>
 *
 * @author Cerex Engineering Team
 * @version 1.0.0
 */
@Entity
@Table(
    name = "users",
    schema = "users_schema",
    indexes = {
        @Index(name = "idx_users_email",        columnList = "email",   unique = true),
        @Index(name = "idx_users_phone",        columnList = "phone",   unique = true),
        @Index(name = "idx_users_oauth",        columnList = "oauth_provider, oauth_provider_id"),
        @Index(name = "idx_users_status",       columnList = "status"),
        @Index(name = "idx_users_created_at",   columnList = "created_at"),
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"passwordHash", "mfaSecret", "profile", "badges", "subscriptions"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    // ─────────────────────────────────────────────────────────────
    // Primary Key
    // ─────────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    // ─────────────────────────────────────────────────────────────
    // Authentication Credentials
    // ─────────────────────────────────────────────────────────────

    /**
     * Primary email address (case-insensitive unique constraint in DB).
     * Max 320 chars per RFC 5321.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 320)
    @Column(name = "email", nullable = false, unique = true, length = 320)
    private String email;

    /** Whether the user has confirmed their email address. */
    @Column(name = "email_verified")
    @Builder.Default
    private Boolean emailVerified = false;

    /** Timestamp when email was verified. */
    @Column(name = "email_verified_at")
    private Instant emailVerifiedAt;

    /**
     * Mobile phone number in E.164 format (e.g., "+33612345678").
     * Optional — used for MFA SMS and SMS notifications.
     */
    @Size(max = 20)
    @Column(name = "phone", unique = true, length = 20)
    private String phone;

    /** Whether the phone number has been SMS-verified. */
    @Column(name = "phone_verified")
    @Builder.Default
    private Boolean phoneVerified = false;

    /**
     * BCrypt-hashed password. Null for OAuth2-only accounts.
     * Uses BCryptPasswordEncoder with strength 12.
     */
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    /** Password salt (BCrypt includes salt, kept for legacy compatibility). */
    @Column(name = "salt", length = 64)
    private String salt;

    // ─────────────────────────────────────────────────────────────
    // OAuth2 Identity
    // ─────────────────────────────────────────────────────────────

    /**
     * OAuth2 provider name.
     * Values: "google", "facebook", "apple"
     */
    @Size(max = 50)
    @Column(name = "oauth_provider", length = 50)
    private String oauthProvider;

    /**
     * Unique ID from the OAuth2 provider (sub claim in OIDC).
     * Used to link OAuth2 logins to existing accounts.
     */
    @Size(max = 255)
    @Column(name = "oauth_provider_id", length = 255)
    private String oauthProviderId;

    // ─────────────────────────────────────────────────────────────
    // Authorization
    // ─────────────────────────────────────────────────────────────

    /**
     * Primary platform role.
     * @see UserRole
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    @Builder.Default
    private UserRole role = UserRole.USER;

    /**
     * Account status controlling access.
     * @see UserStatus
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    // ─────────────────────────────────────────────────────────────
    // Login Security
    // ─────────────────────────────────────────────────────────────

    /** Timestamp of the most recent successful login. */
    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    /** IP address of the most recent successful login (for audit). */
    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    /**
     * Count of consecutive failed login attempts.
     * Resets to 0 on successful login.
     * Account locks at 10 failed attempts.
     */
    @Column(name = "failed_login_count")
    @Builder.Default
    private Integer failedLoginCount = 0;

    /**
     * Timestamp until which account is locked.
     * Null = not locked. Set on account lockout.
     */
    @Column(name = "locked_until")
    private Instant lockedUntil;

    // ─────────────────────────────────────────────────────────────
    // Multi-Factor Authentication (MFA)
    // ─────────────────────────────────────────────────────────────

    /** Whether MFA (TOTP) is enabled for this account. */
    @Column(name = "mfa_enabled")
    @Builder.Default
    private Boolean mfaEnabled = false;

    /**
     * Base32-encoded TOTP secret key (encrypted at rest via JPA converter).
     * Only present when {@link #mfaEnabled} is true.
     */
    @Column(name = "mfa_secret", length = 64)
    private String mfaSecret;

    // ─────────────────────────────────────────────────────────────
    // GDPR & Compliance
    // ─────────────────────────────────────────────────────────────

    /** Whether the user has accepted the Terms of Service and Privacy Policy. */
    @Column(name = "gdpr_consent", nullable = false)
    @Builder.Default
    private Boolean gdprConsent = false;

    /** Timestamp when GDPR consent was given. */
    @Column(name = "gdpr_consent_date")
    private Instant gdprConsentDate;

    /** Whether the user has opted in to marketing communications. */
    @Column(name = "marketing_consent")
    @Builder.Default
    private Boolean marketingConsent = false;

    /**
     * Timestamp when the user requested account deletion (Art. 17 GDPR).
     * Account will be fully deleted 30 days after this date.
     */
    @Column(name = "data_deletion_requested_at")
    private Instant dataDeletionRequestedAt;

    // ─────────────────────────────────────────────────────────────
    // Timestamps
    // ─────────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Soft delete — marks account as deleted without removing the row. */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    // ─────────────────────────────────────────────────────────────
    // Relationships
    // ─────────────────────────────────────────────────────────────

    /** Extended profile information (display name, bio, preferences). */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserProfile profile;

    /** Current and past premium subscriptions. */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("createdAt DESC")
    @Builder.Default
    private Set<Subscription> subscriptions = new HashSet<>();

    /** Achievement and eco badges earned. */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_badges",
        schema = "users_schema",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "badge_id")
    )
    @Builder.Default
    private Set<EcoBadge> badges = new HashSet<>();

    // ─────────────────────────────────────────────────────────────
    // Domain Methods
    // ─────────────────────────────────────────────────────────────

    /**
     * Checks whether this account is currently locked due to failed login attempts.
     *
     * @return true if the account is locked and the lockout period has not expired
     */
    public boolean isAccountLocked() {
        return lockedUntil != null && Instant.now().isBefore(lockedUntil);
    }

    /**
     * Checks whether the account is active and usable.
     *
     * @return true if status is ACTIVE and account is not locked or deleted
     */
    public boolean isActive() {
        return status == UserStatus.ACTIVE
            && deletedAt == null
            && !isAccountLocked();
    }

    /**
     * Records a failed login attempt. Locks account after 10 failures.
     * Each lock doubles in duration: 15min, 30min, 1hr, 2hr, 24hr.
     */
    public void recordFailedLogin() {
        this.failedLoginCount++;
        if (this.failedLoginCount >= 10) {
            long lockMinutes = Math.min(
                15L * (long) Math.pow(2, this.failedLoginCount - 10),
                1440L  // max 24 hours
            );
            this.lockedUntil = Instant.now().plusSeconds(lockMinutes * 60);
        }
    }

    /**
     * Records a successful login. Resets failed attempt counter.
     *
     * @param ipAddress the IP address of the login request
     */
    public void recordSuccessfulLogin(String ipAddress) {
        this.lastLoginAt = Instant.now();
        this.lastLoginIp = ipAddress;
        this.failedLoginCount = 0;
        this.lockedUntil = null;
    }

    /**
     * Returns the user's active subscription plan, if any.
     *
     * @return the active subscription, or null if no active subscription exists
     */
    public Subscription getActiveSubscription() {
        return subscriptions.stream()
            .filter(s -> "ACTIVE".equals(s.getStatus()))
            .findFirst()
            .orElse(null);
    }

    /**
     * Returns the subscription plan name for the active subscription.
     *
     * @return the plan name ("FREE", "EXPLORER", "CHEF_PRO", "ENTERPRISE")
     */
    public String getSubscriptionPlan() {
        Subscription active = getActiveSubscription();
        return active != null ? active.getPlan() : "FREE";
    }

    /**
     * Awards a badge to this user.
     *
     * @param badge the badge to award
     */
    public void awardBadge(EcoBadge badge) {
        this.badges.add(badge);
    }

    /**
     * Marks the user account as pending deletion (GDPR Art. 17).
     * Full deletion will occur after a 30-day grace period.
     */
    public void requestDeletion() {
        this.dataDeletionRequestedAt = Instant.now();
        this.status = UserStatus.PENDING_DELETION;
    }

    // ─────────────────────────────────────────────────────────────
    // Enums
    // ─────────────────────────────────────────────────────────────

    /**
     * Platform roles determining feature access and permissions.
     *
     * <p>Hierarchy: SUPER_ADMIN > ADMIN > MODERATOR > RESTAURANT_OWNER > CHEF > USER
     */
    public enum UserRole {
        /** Standard registered user. Can discover, save, and order recipes. */
        USER,

        /** Professional chef. Can create unlimited recipes and monetize content. */
        CHEF,

        /** Restaurant business owner. Can manage restaurant listings and menus. */
        RESTAURANT_OWNER,

        /** Content moderator. Can review and approve/reject recipes. */
        MODERATOR,

        /** Platform administrator. Full user and content management. */
        ADMIN,

        /** Super administrator. Full system access including configuration. */
        SUPER_ADMIN
    }

    /**
     * Account status controlling platform access.
     */
    public enum UserStatus {
        /** Account is fully active — normal access granted. */
        ACTIVE,

        /** Account registration pending email verification. */
        PENDING,

        /** Account temporarily suspended (timeboxed). */
        SUSPENDED,

        /** Account permanently banned for serious violations. */
        BANNED,

        /** Account deletion has been requested (30-day grace period). */
        PENDING_DELETION,

        /** Account has been deleted (anonymized data may remain). */
        DELETED
    }
}
