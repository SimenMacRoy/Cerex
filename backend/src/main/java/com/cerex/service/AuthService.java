package com.cerex.service;

import com.cerex.domain.User;
import com.cerex.domain.UserProfile;
import com.cerex.dto.auth.AuthResponse;
import com.cerex.dto.auth.LoginRequest;
import com.cerex.dto.auth.RegisterRequest;
import com.cerex.exception.BusinessException;
import com.cerex.exception.DuplicateResourceException;
import com.cerex.repository.UserProfileRepository;
import com.cerex.repository.UserRepository;
import com.cerex.security.CerexUserDetails;
import com.cerex.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Service layer for authentication: registration, login, token refresh, logout.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Register a new user account.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        if (!request.isGdprConsent()) {
            throw new BusinessException("GDPR_CONSENT_REQUIRED",
                "GDPR consent is required to create an account");
        }

        // Create user entity
        User user = User.builder()
            .email(request.getEmail().toLowerCase().trim())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .role(User.UserRole.USER)
            .status(User.UserStatus.ACTIVE)
            .emailVerified(false)
            .gdprConsent(true)
            .gdprConsentDate(Instant.now())
            .marketingConsent(request.isMarketingConsent())
            .build();

        user = userRepository.save(user);

        // Create profile
        UserProfile profile = UserProfile.builder()
            .user(user)
            .displayName(request.getDisplayName())
            .preferredLanguage(request.getPreferredLanguage() != null ? request.getPreferredLanguage() : "en")
            .preferredCurrency(request.getPreferredCurrency() != null ? request.getPreferredCurrency() : "EUR")
            .build();

        profileRepository.save(profile);

        log.info("New user registered: {} ({})", user.getId(), user.getEmail());

        // Publish event
        try {
            kafkaTemplate.send("cerex.user.created", user.getId().toString(), user.getId());
        } catch (Exception e) {
            log.warn("Failed to publish user.created event: {}", e.getMessage());
        }

        // Generate tokens
        CerexUserDetails userDetails = new CerexUserDetails(user);
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(3600)
            .user(AuthResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(profile.getDisplayName())
                .role(user.getRole().name())
                .subscriptionPlan("FREE")
                .createdAt(user.getCreatedAt())
                .build())
            .build();
    }

    /**
     * Authenticate a user with email and password.
     */
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail().toLowerCase().trim(),
                request.getPassword()
            )
        );

        CerexUserDetails userDetails = (CerexUserDetails) authentication.getPrincipal();

        // Record successful login
        User user = userRepository.findById(userDetails.getUserId())
            .orElseThrow();
        user.recordSuccessfulLogin(ipAddress);
        userRepository.save(user);

        UserProfile profile = profileRepository.findByUserId(user.getId()).orElse(null);

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        log.info("User logged in: {} from IP {}", user.getId(), ipAddress);

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(3600)
            .user(AuthResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(profile != null ? profile.getDisplayName() : user.getEmail())
                .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
                .role(user.getRole().name())
                .subscriptionPlan(user.getSubscriptionPlan())
                .createdAt(user.getCreatedAt())
                .build())
            .build();
    }

    /**
     * Refresh an access token using a valid refresh token.
     */
    public AuthResponse refreshToken(String refreshToken) {
        var userId = jwtService.extractUserId(refreshToken);
        if (jwtService.isTokenExpired(refreshToken)) {
            throw new BusinessException("TOKEN_EXPIRED", "Refresh token has expired");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));

        CerexUserDetails userDetails = new CerexUserDetails(user);
        String newAccessToken = jwtService.generateAccessToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        UserProfile profile = profileRepository.findByUserId(user.getId()).orElse(null);

        return AuthResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken)
            .tokenType("Bearer")
            .expiresIn(3600)
            .user(AuthResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(profile != null ? profile.getDisplayName() : user.getEmail())
                .role(user.getRole().name())
                .subscriptionPlan(user.getSubscriptionPlan())
                .build())
            .build();
    }
}
