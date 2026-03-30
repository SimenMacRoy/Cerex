package com.cerex.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Premium subscription management entity.
 *
 * <p>Supports plans: FREE, EXPLORER (€9.99/mo), CHEF_PRO (€24.99/mo), ENTERPRISE.
 * Integrates with Stripe for billing lifecycle.
 *
 * @author Cerex Engineering Team
 * @version 1.0.0
 */
@Entity
@Table(
    name = "subscriptions",
    schema = "subscriptions_schema",
    indexes = {
        @Index(name = "idx_subscriptions_user_id",   columnList = "user_id"),
        @Index(name = "idx_subscriptions_status",    columnList = "status"),
        @Index(name = "idx_subscriptions_stripe_id", columnList = "stripe_subscription_id"),
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "user_id", insertable = false, updatable = false)
    private UUID userId;

    /** Plan name: FREE, EXPLORER, CHEF_PRO, ENTERPRISE */
    @NotBlank
    @Column(name = "plan", nullable = false, length = 30)
    private String plan;

    /** Subscription status: ACTIVE, CANCELLED, EXPIRED, PAST_DUE, TRIALING */
    @NotBlank
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    /** Billing cycle: MONTHLY or ANNUAL */
    @Column(name = "billing_cycle", length = 10)
    @Builder.Default
    private String billingCycle = "MONTHLY";

    /** Monthly or annual price in the user's currency. */
    @DecimalMin("0.00")
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Size(min = 3, max = 3)
    @Column(name = "currency_code", length = 3)
    @Builder.Default
    private String currencyCode = "EUR";

    /** Stripe subscription ID for billing operations. */
    @Size(max = 200)
    @Column(name = "stripe_subscription_id", length = 200)
    private String stripeSubscriptionId;

    /** Stripe customer ID linked to this user. */
    @Size(max = 200)
    @Column(name = "stripe_customer_id", length = 200)
    private String stripeCustomerId;

    @Column(name = "trial_ends_at")
    private Instant trialEndsAt;

    @Column(name = "current_period_start")
    private Instant currentPeriodStart;

    @Column(name = "current_period_end")
    private Instant currentPeriodEnd;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // ── Domain Methods ──────────────────────────────────────
    public boolean isActive() {
        return "ACTIVE".equals(status) || "TRIALING".equals(status);
    }

    public boolean isTrialing() {
        return "TRIALING".equals(status) && trialEndsAt != null && Instant.now().isBefore(trialEndsAt);
    }

    public void cancel() {
        this.status = "CANCELLED";
        this.cancelledAt = Instant.now();
    }
}
