package com.cerex.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Domain entity representing a menu belonging to a restaurant.
 *
 * <p>A restaurant may have multiple menus (e.g., Breakfast, Lunch, Dinner, Weekend Special).
 * Each menu contains a list of {@link MenuItem} entries.
 *
 * @author Cerex Engineering Team
 * @version 1.0.0
 */
@Entity
@Table(
    name = "menus",
    schema = "orders_schema",
    indexes = {
        @Index(name = "idx_menus_restaurant_id", columnList = "restaurant_id"),
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"restaurant", "items"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @NotBlank
    @Size(max = 200)
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "menu_type", length = 30)
    @Builder.Default
    private MenuType menuType = MenuType.REGULAR;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "available_from", length = 10)
    private String availableFrom; // "08:00"

    @Column(name = "available_until", length = 10)
    private String availableUntil; // "11:30"

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("category ASC, sortOrder ASC")
    @Builder.Default
    private List<MenuItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum MenuType {
        REGULAR,
        BREAKFAST,
        LUNCH,
        DINNER,
        BRUNCH,
        HAPPY_HOUR,
        SPECIAL,
        SEASONAL,
        CATERING
    }

    // ─────────────────────────────────────────────────────────
    // Domain Methods
    // ─────────────────────────────────────────────────────────

    public void addItem(MenuItem item) {
        items.add(item);
        item.setMenu(this);
    }

    public void removeItem(MenuItem item) {
        items.remove(item);
        item.setMenu(null);
    }

    public List<MenuItem> getAvailableItems() {
        return items.stream()
            .filter(MenuItem::getIsAvailable)
            .toList();
    }

    public int getItemCount() {
        return items.size();
    }
}
