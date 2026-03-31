package com.cerex.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for authentication operations (login, register, token refresh).
 * The {@code user} field is shaped to match the frontend's {@code UserProfile} type.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private UserInfo user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserInfo {
        private UUID id;
        private String email;
        private String firstName;
        private String lastName;
        private String username;
        private String avatarUrl;
        private String bio;
        private String role;
        private String status;
        private String preferredLanguage;
        private int followersCount;
        private int followingCount;
        private int recipesCount;
        private List<Object> ecoBadges;
        private String subscriptionPlan;
        private Instant createdAt;
    }
}
