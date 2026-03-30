package com.cerex.controller;

import com.cerex.dto.ApiResponse;
import com.cerex.dto.user.UpdateProfileRequest;
import com.cerex.dto.user.UserProfileDTO;
import com.cerex.security.CerexUserDetails;
import com.cerex.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for user profile management.
 *
 * <p>Base path: {@code /api/v1/users}
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile management")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getCurrentUser(
            @AuthenticationPrincipal CerexUserDetails currentUser) {
        UserProfileDTO profile = userService.getUserProfile(currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @PatchMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse<UserProfileDTO>> updateProfile(
            @AuthenticationPrincipal CerexUserDetails currentUser,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileDTO profile = userService.updateProfile(currentUser.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok(profile, "Profile updated successfully"));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get public user profile")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getPublicProfile(
            @PathVariable UUID userId) {
        UserProfileDTO profile = userService.getPublicProfile(userId);
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @DeleteMapping("/me")
    @Operation(summary = "Request GDPR account deletion (30-day grace period)")
    public ResponseEntity<ApiResponse<String>> deleteAccount(
            @AuthenticationPrincipal CerexUserDetails currentUser) {
        userService.requestAccountDeletion(currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(
            "Account deletion scheduled. Your data will be permanently deleted in 30 days.",
            "Account deletion requested"
        ));
    }
}
