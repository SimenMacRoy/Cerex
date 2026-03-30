package com.cerex.controller;

import com.cerex.dto.ApiResponse;
import com.cerex.dto.restaurant.GroceryDTO;
import com.cerex.security.CerexUserDetails;
import com.cerex.service.GroceryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.UUID;

/**
 * REST controller for grocery store management.
 */
@RestController
@RequestMapping("/api/v1/groceries")
@RequiredArgsConstructor
@Tag(name = "Groceries", description = "Grocery store & product discovery APIs")
public class GroceryController {

    private final GroceryService groceryService;

    // ─────────────────────────────────────────────────────────
    // PUBLIC ENDPOINTS
    // ─────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Lister les épiceries actives")
    public ResponseEntity<ApiResponse<Page<GroceryDTO>>> listGroceries(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
            groceryService.listActiveGroceries(pageable)));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Détails d'une épicerie par slug")
    public ResponseEntity<ApiResponse<GroceryDTO>> getGrocery(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(
            groceryService.getGroceryBySlug(slug)));
    }

    @GetMapping("/id/{id}")
    @Operation(summary = "Détails d'une épicerie par ID")
    public ResponseEntity<ApiResponse<GroceryDTO>> getGroceryById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
            groceryService.getGroceryById(id)));
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des épiceries")
    public ResponseEntity<ApiResponse<Page<GroceryDTO>>> searchGroceries(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
            groceryService.searchGroceries(query, pageable)));
    }

    @GetMapping("/nearby")
    @Operation(summary = "Épiceries à proximité")
    public ResponseEntity<ApiResponse<Page<GroceryDTO>>> findNearby(
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lng,
            @RequestParam(defaultValue = "15") double radiusKm,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
            groceryService.findNearby(lat, lng, radiusKm, pageable)));
    }

    @GetMapping("/city/{city}")
    @Operation(summary = "Épiceries par ville")
    public ResponseEntity<ApiResponse<Page<GroceryDTO>>> findByCity(
            @PathVariable String city,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
            groceryService.findByCity(city, pageable)));
    }

    @GetMapping("/organic")
    @Operation(summary = "Épiceries bio certifiées")
    public ResponseEntity<ApiResponse<Page<GroceryDTO>>> findOrganic(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
            groceryService.findOrganic(pageable)));
    }

    // ─────────────────────────────────────────────────────────
    // OWNER ENDPOINTS
    // ─────────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Créer une épicerie")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<GroceryDTO>> createGrocery(
            @AuthenticationPrincipal CerexUserDetails user,
            @RequestBody GroceryDTO request) {
        GroceryDTO dto = groceryService.createGrocery(user.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(dto));
    }

    @GetMapping("/me")
    @Operation(summary = "Mes épiceries")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<GroceryDTO>>> getMyGroceries(
            @AuthenticationPrincipal CerexUserDetails user,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
            groceryService.getMyGroceries(user.getUserId(), pageable)));
    }

    // ─────────────────────────────────────────────────────────
    // ADMIN ENDPOINTS
    // ─────────────────────────────────────────────────────────

    @PatchMapping("/admin/{groceryId}/verify")
    @Operation(summary = "Vérifier une épicerie (Admin)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<GroceryDTO>> verifyGrocery(
            @PathVariable UUID groceryId) {
        return ResponseEntity.ok(ApiResponse.success(
            groceryService.verifyGrocery(groceryId)));
    }
}
