package com.cerex.controller;

import com.cerex.dto.ApiResponse;
import com.cerex.dto.restaurant.*;
import com.cerex.security.CerexUserDetails;
import com.cerex.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for restaurant management.
 *
 * <p>Provides endpoints for:
 * <ul>
 *   <li>Public browsing (search, nearby, cuisine filter)</li>
 *   <li>Owner management (CRUD, menu management)</li>
 *   <li>Admin moderation (verify, suspend)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
@Tag(name = "Restaurants", description = "Restaurant management & discovery APIs")
public class RestaurantController {

    private final RestaurantService restaurantService;

    // ─────────────────────────────────────────────────────────
    // PUBLIC ENDPOINTS
    // ─────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Lister les restaurants actifs")
    public ResponseEntity<ApiResponse<Page<RestaurantDTO>>> listRestaurants(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
            restaurantService.listActiveRestaurants(pageable)));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Détails d'un restaurant par slug")
    public ResponseEntity<ApiResponse<RestaurantDTO>> getRestaurant(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(
            restaurantService.getRestaurantBySlug(slug)));
    }

    @GetMapping("/id/{id}")
    @Operation(summary = "Détails d'un restaurant par ID")
    public ResponseEntity<ApiResponse<RestaurantDTO>> getRestaurantById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
            restaurantService.getRestaurantById(id)));
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des restaurants")
    public ResponseEntity<ApiResponse<Page<RestaurantDTO>>> searchRestaurants(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
            restaurantService.searchRestaurants(query, pageable)));
    }

    @GetMapping("/nearby")
    @Operation(summary = "Restaurants à proximité")
    public ResponseEntity<ApiResponse<Page<RestaurantDTO>>> findNearby(
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lng,
            @RequestParam(defaultValue = "10") double radiusKm,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
            restaurantService.findNearby(lat, lng, radiusKm, pageable)));
    }

    @GetMapping("/city/{city}")
    @Operation(summary = "Restaurants par ville")
    public ResponseEntity<ApiResponse<Page<RestaurantDTO>>> findByCity(
            @PathVariable String city,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
            restaurantService.findByCity(city, pageable)));
    }

    @GetMapping("/cuisine/{cuisine}")
    @Operation(summary = "Restaurants par cuisine")
    public ResponseEntity<ApiResponse<Page<RestaurantDTO>>> findByCuisine(
            @PathVariable String cuisine,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
            restaurantService.findByCuisine(cuisine, pageable)));
    }

    @GetMapping("/top-rated")
    @Operation(summary = "Restaurants les mieux notés")
    public ResponseEntity<ApiResponse<Page<RestaurantDTO>>> findTopRated(
            @RequestParam(defaultValue = "5") int minReviews,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
            restaurantService.findTopRated(minReviews, pageable)));
    }

    @GetMapping("/eco-friendly")
    @Operation(summary = "Restaurants éco-responsables")
    public ResponseEntity<ApiResponse<Page<RestaurantDTO>>> findEcoFriendly(
            @RequestParam(defaultValue = "70") int minScore,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
            restaurantService.findEcoFriendly(minScore, pageable)));
    }

    @GetMapping("/{restaurantId}/menus")
    @Operation(summary = "Menus d'un restaurant")
    public ResponseEntity<ApiResponse<List<MenuDTO>>> getMenus(@PathVariable UUID restaurantId) {
        return ResponseEntity.ok(ApiResponse.success(
            restaurantService.getRestaurantMenus(restaurantId)));
    }

    // ─────────────────────────────────────────────────────────
    // OWNER ENDPOINTS
    // ─────────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Créer un restaurant")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RestaurantDTO>> createRestaurant(
            @AuthenticationPrincipal CerexUserDetails user,
            @Valid @RequestBody CreateRestaurantRequest request) {
        RestaurantDTO dto = restaurantService.createRestaurant(user.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(dto));
    }

    @PatchMapping("/{restaurantId}")
    @Operation(summary = "Mettre à jour un restaurant")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RestaurantDTO>> updateRestaurant(
            @AuthenticationPrincipal CerexUserDetails user,
            @PathVariable UUID restaurantId,
            @Valid @RequestBody CreateRestaurantRequest request) {
        RestaurantDTO dto = restaurantService.updateRestaurant(user.getUserId(), restaurantId, request);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/me")
    @Operation(summary = "Mes restaurants")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<RestaurantDTO>>> getMyRestaurants(
            @AuthenticationPrincipal CerexUserDetails user,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
            restaurantService.getMyRestaurants(user.getUserId(), pageable)));
    }

    // Menu Management

    @PostMapping("/{restaurantId}/menus")
    @Operation(summary = "Ajouter un menu")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MenuDTO>> addMenu(
            @AuthenticationPrincipal CerexUserDetails user,
            @PathVariable UUID restaurantId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String menuType) {
        MenuDTO dto = restaurantService.addMenu(user.getUserId(), restaurantId, name, description, menuType);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(dto));
    }

    @PostMapping("/{restaurantId}/menus/{menuId}/items")
    @Operation(summary = "Ajouter un article au menu")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MenuItemDTO>> addMenuItem(
            @AuthenticationPrincipal CerexUserDetails user,
            @PathVariable UUID restaurantId,
            @PathVariable UUID menuId,
            @Valid @RequestBody CreateMenuItemRequest request) {
        MenuItemDTO dto = restaurantService.addMenuItem(user.getUserId(), restaurantId, menuId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(dto));
    }

    // ─────────────────────────────────────────────────────────
    // ADMIN ENDPOINTS
    // ─────────────────────────────────────────────────────────

    @PatchMapping("/admin/{restaurantId}/verify")
    @Operation(summary = "Vérifier un restaurant (Admin)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RestaurantDTO>> verifyRestaurant(
            @PathVariable UUID restaurantId) {
        return ResponseEntity.ok(ApiResponse.success(
            restaurantService.verifyRestaurant(restaurantId)));
    }

    @PatchMapping("/admin/{restaurantId}/suspend")
    @Operation(summary = "Suspendre un restaurant (Admin)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RestaurantDTO>> suspendRestaurant(
            @PathVariable UUID restaurantId) {
        return ResponseEntity.ok(ApiResponse.success(
            restaurantService.suspendRestaurant(restaurantId)));
    }
}
