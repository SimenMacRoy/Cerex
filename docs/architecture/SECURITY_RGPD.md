# Cerex — Security & GDPR Compliance

> Version: 1.0.0 | Last Updated: 2026-03-30 | Classification: CONFIDENTIAL

---

## Table of Contents

1. [Security Architecture Overview](#1-security-architecture-overview)
2. [JWT + OAuth2 Implementation](#2-jwt--oauth2-implementation)
3. [Role-Based Access Control (RBAC)](#3-role-based-access-control-rbac)
4. [Data Encryption Strategy](#4-data-encryption-strategy)
5. [GDPR Compliance Checklist](#5-gdpr-compliance-checklist)
6. [API Security](#6-api-security)
7. [Fraud Detection for Restaurants](#7-fraud-detection-for-restaurants)
8. [Data Retention Policies](#8-data-retention-policies)
9. [Security Monitoring](#9-security-monitoring)
10. [Incident Response Plan](#10-incident-response-plan)

---

## 1. Security Architecture Overview

### Defense in Depth

```
┌─────────────────────────────────────────────────────────────┐
│                  SECURITY LAYERS                            │
│                                                             │
│  Layer 1: Network Security                                  │
│  ├── CloudFlare WAF (DDoS, SQL injection, XSS filtering)    │
│  ├── VPC with private subnets for backend services          │
│  └── Security Groups: minimal port exposure                 │
│                                                             │
│  Layer 2: API Gateway Security                              │
│  ├── TLS 1.3 termination (no HTTP allowed)                  │
│  ├── Rate limiting by IP and user                           │
│  ├── Kong JWT validation plugin                             │
│  └── CORS enforcement                                       │
│                                                             │
│  Layer 3: Application Security                              │
│  ├── Spring Security filter chain                           │
│  ├── JWT signature verification (RS256)                     │
│  ├── Method-level authorization (@PreAuthorize)             │
│  └── Input validation (Bean Validation + custom)            │
│                                                             │
│  Layer 4: Service Mesh Security                             │
│  ├── Istio mutual TLS (mTLS) between all services           │
│  ├── Service-to-service authorization policies              │
│  └── Certificate rotation (SPIFFE/SPIRE)                    │
│                                                             │
│  Layer 5: Data Security                                     │
│  ├── Encryption at rest (AES-256)                           │
│  ├── Encryption in transit (TLS 1.3)                        │
│  ├── Database field-level encryption for PII                │
│  └── Key management via AWS KMS                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. JWT + OAuth2 Implementation

### 2.1 JWT Token Structure

```
Header:  { "alg": "RS256", "typ": "JWT", "kid": "cerex-key-2026-v1" }

Payload: {
  "sub":   "550e8400-e29b-41d4-a716-446655440000",  // userId
  "email": "user@example.com",
  "role":  "CHEF",
  "sub_plan": "CHEF_PRO",                            // subscription plan
  "iat":   1743000000,                               // issued at
  "exp":   1743003600,                               // expires in 1 hour
  "jti":   "token-uuid-123",                        // JWT ID for revocation
  "iss":   "https://auth.cerex.com",
  "aud":   ["cerex-api", "cerex-mobile"],
  "cerex": {
    "verified_chef": true,
    "locale": "fr_FR",
    "mfa_verified": true
  }
}

Signature: RS256(base64(header) + "." + base64(payload), privateKey)
```

### 2.2 Token Lifecycle

```
1. Login/Register
   → Generate access_token (1 hour TTL)
   → Generate refresh_token (30 days TTL, stored in HttpOnly cookie)
   → Store refresh_token hash in Redis with metadata

2. API Request
   → Client sends: Authorization: Bearer <access_token>
   → API Gateway validates signature
   → Spring Security extracts claims
   → Request proceeds if valid

3. Token Refresh (before expiry)
   → Client sends refresh_token
   → Validate against Redis (check not revoked)
   → Issue new access_token
   → Optionally rotate refresh_token (refresh token rotation)

4. Logout
   → Add access_token JTI to Redis blocklist (TTL = remaining token lifetime)
   → Delete refresh_token from Redis
   → Clear HttpOnly cookie
```

### 2.3 Spring Security Configuration

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final OAuth2UserService oAuth2UserService;
    private final CorsProperties corsProperties;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            // Stateless — no HTTP session
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // CSRF disabled (JWT-based, no cookies for auth)
            .csrf(AbstractHttpConfigurer::disable)

            // CORS configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Security headers
            .headers(headers -> headers
                .frameOptions(FrameOptionsConfig::deny)
                .contentTypeOptions(withDefaults())
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000)
                )
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; " +
                    "img-src 'self' https://media.cerex.com; " +
                    "script-src 'self'; " +
                    "style-src 'self' 'unsafe-inline';"
                ))
            )

            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/v1/recipes/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/v1/search/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/v1/explore/**").permitAll()
                .requestMatchers("/actuator/health/**").permitAll()

                // Authenticated endpoints
                .requestMatchers(HttpMethod.POST, "/api/v1/recipes/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/orders/**").authenticated()
                .requestMatchers(HttpMethod.GET,  "/api/v1/users/me/**").authenticated()

                // Admin-only endpoints
                .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                // Everything else requires authentication
                .anyRequest().authenticated()
            )

            // OAuth2 login
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(ui -> ui.userService(oAuth2UserService))
                .successHandler(oAuth2AuthenticationSuccessHandler())
                .failureHandler(oAuth2AuthenticationFailureHandler())
            )

            // JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

            // Exception handling
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint())
                .accessDeniedHandler(jwtAccessDeniedHandler())
            )

            .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(corsProperties.getAllowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Request-ID",
                                         "X-Cerex-Client", "Accept-Language"));
        config.setExposedHeaders(List.of("X-RateLimit-Limit", "X-RateLimit-Remaining",
                                         "X-RateLimit-Reset", "X-Request-ID"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}

// ─────────────────────────────────────────────────────────────
// JwtAuthenticationFilter.java
// ─────────────────────────────────────────────────────────────
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenBlocklistService blocklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            // Verify token is not in blocklist (revoked/logged-out)
            if (blocklistService.isBlocked(token)) {
                throw new TokenRevokedException("Token has been revoked");
            }

            Claims claims = jwtService.validateAndExtractClaims(token);
            String userId = claims.getSubject();
            String role = claims.get("role", String.class);

            // Build authentication object
            UserPrincipal principal = UserPrincipal.fromClaims(claims);
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (ExpiredJwtException e) {
            log.debug("JWT token expired: {}", e.getMessage());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":{\"code\":\"TOKEN_EXPIRED\"}}");
            return;
        } catch (JwtException | TokenRevokedException e) {
            log.warn("JWT token invalid: {}", e.getMessage());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        filterChain.doFilter(request, response);
    }
}
```

### 2.4 OAuth2 Integration (Google, Facebook, Apple)

```java
@Service
@RequiredArgsConstructor
public class CerexOAuth2UserService implements DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);
        String provider = request.getClientRegistration().getRegistrationId();

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(provider, oAuth2User.getAttributes());

        // Find existing user or create new one
        User user = userRepository.findByOAuthProviderAndProviderId(provider, userInfo.getId())
            .orElseGet(() -> createOAuthUser(userInfo, provider));

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    private User createOAuthUser(OAuth2UserInfo userInfo, String provider) {
        User user = User.builder()
            .email(userInfo.getEmail())
            .emailVerified(true)
            .oauthProvider(provider)
            .oauthProviderId(userInfo.getId())
            .role(UserRole.USER)
            .status(UserStatus.ACTIVE)
            .gdprConsent(false)  // Must obtain explicit consent on first login
            .build();
        return userRepository.save(user);
    }
}
```

---

## 3. Role-Based Access Control (RBAC)

### 3.1 Role Hierarchy

```
SUPER_ADMIN
    └── ADMIN
         ├── MODERATOR
         └── RESTAURANT_OWNER
              └── CHEF
                   └── USER (default)
```

### 3.2 Permission Matrix

| Permission | USER | CHEF | RESTAURANT_OWNER | MODERATOR | ADMIN | SUPER_ADMIN |
|-----------|------|------|-----------------|-----------|-------|-------------|
| Read public recipes | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Create recipe | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Publish own recipe | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Edit own recipe | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Delete own recipe | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Place order | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Manage restaurant menu | ✗ | ✗ | ✓ | ✗ | ✓ | ✓ |
| View restaurant analytics | ✗ | ✗ | ✓ | ✗ | ✓ | ✓ |
| Moderate content | ✗ | ✗ | ✗ | ✓ | ✓ | ✓ |
| Manage users | ✗ | ✗ | ✗ | ✗ | ✓ | ✓ |
| View admin analytics | ✗ | ✗ | ✗ | ✗ | ✓ | ✓ |
| Manage system config | ✗ | ✗ | ✗ | ✗ | ✗ | ✓ |
| Delete any content | ✗ | ✗ | ✗ | ✓ | ✓ | ✓ |
| Suspend users | ✗ | ✗ | ✗ | ✗ | ✓ | ✓ |

### 3.3 Method-Level Security

```java
@Service
public class RecipeService {

    // Only the recipe author can edit
    @PreAuthorize("@recipeSecurityService.isAuthor(#recipeId, authentication)")
    public RecipeDetailDTO updateRecipe(UUID recipeId, UpdateRecipeRequest request) { ... }

    // Only admins and moderators can approve
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'SUPER_ADMIN')")
    public void approveRecipe(UUID recipeId, UUID moderatorId) { ... }

    // Only the author OR admin can delete
    @PreAuthorize("@recipeSecurityService.isAuthor(#recipeId, authentication) " +
                  "or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public void deleteRecipe(UUID recipeId) { ... }

    // Restaurant owners can only manage their own restaurants
    @PreAuthorize("hasRole('RESTAURANT_OWNER') " +
                  "and @restaurantSecurityService.isOwner(#restaurantId, authentication)")
    public void updateRestaurantMenu(UUID restaurantId, UpdateMenuRequest request) { ... }
}

// Security helper component
@Component("recipeSecurityService")
@RequiredArgsConstructor
public class RecipeSecurityService {

    private final RecipeRepository recipeRepository;

    public boolean isAuthor(UUID recipeId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return false;
        UserPrincipal user = (UserPrincipal) auth.getPrincipal();
        return recipeRepository.existsByIdAndAuthorId(recipeId, user.getId());
    }
}
```

---

## 4. Data Encryption Strategy

### 4.1 Encryption Overview

| Data Type | Storage | Encryption | Key Manager |
|-----------|---------|-----------|-------------|
| Passwords | PostgreSQL | bcrypt (cost=12) | N/A (one-way) |
| Payment data | Stripe (never stored) | Stripe encryption | Stripe |
| Personal data (name, email) | PostgreSQL | AES-256 at field level | AWS KMS |
| Media files | S3 | AES-256 SSE | AWS KMS |
| Database volumes | EBS/RDS | AES-256 | AWS KMS |
| Redis data | ElastiCache | AES-256 in transit | AWS KMS |
| Kafka messages | MSK | TLS 1.3 | AWS ACM |
| Backups | S3 Glacier | AES-256 | AWS KMS |

### 4.2 Field-Level Encryption for PII

```java
// ─────────────────────────────────────────────────────────────
// FieldEncryptionConverter.java
// JPA AttributeConverter for transparent field encryption
// ─────────────────────────────────────────────────────────────
@Component
@Converter
public class FieldEncryptionConverter implements AttributeConverter<String, String> {

    private final EncryptionService encryptionService;

    @Override
    public String convertToDatabaseColumn(String plaintext) {
        if (plaintext == null) return null;
        return encryptionService.encrypt(plaintext);
    }

    @Override
    public String convertToEntityAttribute(String ciphertext) {
        if (ciphertext == null) return null;
        return encryptionService.decrypt(ciphertext);
    }
}

// Usage in entity:
@Entity
public class UserProfile {
    @Convert(converter = FieldEncryptionConverter.class)
    @Column(name = "phone")
    private String phone;     // Stored encrypted in DB

    @Convert(converter = FieldEncryptionConverter.class)
    @Column(name = "address")
    private String address;
}

// ─────────────────────────────────────────────────────────────
// EncryptionService.java — AWS KMS-backed encryption
// ─────────────────────────────────────────────────────────────
@Service
@RequiredArgsConstructor
@Slf4j
public class EncryptionService {

    private final KmsClient kmsClient;

    @Value("${cerex.encryption.key-id}")
    private String keyId;

    public String encrypt(String plaintext) {
        EncryptRequest request = EncryptRequest.builder()
            .keyId(keyId)
            .plaintext(SdkBytes.fromUtf8String(plaintext))
            .encryptionContext(Map.of("app", "cerex", "env", "prod"))
            .build();

        EncryptResponse response = kmsClient.encrypt(request);
        return Base64.getEncoder().encodeToString(
            response.ciphertextBlob().asByteArray()
        );
    }

    public String decrypt(String ciphertext) {
        DecryptRequest request = DecryptRequest.builder()
            .ciphertextBlob(SdkBytes.fromByteArray(Base64.getDecoder().decode(ciphertext)))
            .encryptionContext(Map.of("app", "cerex", "env", "prod"))
            .build();

        DecryptResponse response = kmsClient.decrypt(request);
        return response.plaintext().asUtf8String();
    }
}
```

### 4.3 Password Security

```java
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt with cost factor 12 (≈ 300ms per hash, adequate for auth)
        return new BCryptPasswordEncoder(12);
    }
}

// Password strength enforcement
@Component
public class PasswordPolicyValidator {

    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGITS    = Pattern.compile("[0-9]");
    private static final Pattern SPECIALS  = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");

    public void validate(String password) {
        if (password.length() < 8)
            throw new WeakPasswordException("Password must be at least 8 characters");
        if (!UPPERCASE.matcher(password).find())
            throw new WeakPasswordException("Password must contain an uppercase letter");
        if (!LOWERCASE.matcher(password).find())
            throw new WeakPasswordException("Password must contain a lowercase letter");
        if (!DIGITS.matcher(password).find())
            throw new WeakPasswordException("Password must contain a digit");

        // Check against known breached passwords (HaveIBeenPwned API)
        checkBreachedPasswords(password);
    }
}
```

---

## 5. GDPR Compliance Checklist

### 5.1 Legal Basis for Processing

| Data Processing Activity | Legal Basis | Retention |
|--------------------------|-------------|-----------|
| Account creation & auth | Contract performance | Duration of account |
| Order processing | Contract performance | 7 years (tax law) |
| Recipe personalization | Legitimate interest | 2 years |
| Marketing emails | Consent | Until withdrawal |
| Analytics & profiling | Legitimate interest | 13 months |
| Fraud prevention | Legitimate interest | 5 years |

### 5.2 Data Subject Rights Implementation

```java
// ─────────────────────────────────────────────────────────────
// GdprService.java — All GDPR data subject rights in one place
// ─────────────────────────────────────────────────────────────
@Service
@RequiredArgsConstructor
@Slf4j
public class GdprService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final RecipeRepository recipeRepository;
    private final S3Service s3Service;
    private final DataExportSerializer serializer;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * RIGHT TO ACCESS (Art. 15 GDPR)
     * Export all personal data for a user in machine-readable format.
     */
    public DataExportResult exportUserData(UUID userId) {
        log.info("GDPR data export requested for user: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        GdprDataExport export = GdprDataExport.builder()
            .exportedAt(Instant.now())
            .userData(user)
            .profile(userRepository.findProfileByUserId(userId).orElse(null))
            .orders(orderRepository.findAllByUserId(userId))
            .recipes(recipeRepository.findAllByAuthorId(userId))
            .reviews(reviewRepository.findAllByReviewerId(userId))
            .socialPosts(socialPostRepository.findAllByAuthorId(userId))
            .build();

        // Serialize to JSON and upload to S3
        String fileName = "cerex-data-export-" + userId + "-" + System.currentTimeMillis() + ".json";
        String exportUrl = s3Service.uploadEncrypted(
            "gdpr-exports/" + fileName,
            serializer.serialize(export)
        );

        // URL expires in 48 hours
        String downloadUrl = s3Service.generatePresignedUrl(exportUrl, Duration.ofHours(48));

        // Notify user
        kafkaTemplate.send("notification.events", GdprExportReadyEvent.of(userId, downloadUrl));

        return DataExportResult.builder()
            .exportId("export-" + UUID.randomUUID())
            .downloadUrl(downloadUrl)
            .expiresAt(Instant.now().plus(Duration.ofHours(48)))
            .build();
    }

    /**
     * RIGHT TO ERASURE (Art. 17 GDPR) — "Right to be Forgotten"
     * Schedules account deletion with 30-day grace period.
     */
    @Transactional
    public DeletionResult requestAccountDeletion(UUID userId, String reason) {
        log.info("GDPR deletion requested for user: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        Instant scheduledDeletion = Instant.now().plus(Duration.ofDays(30));
        user.setDataDeletionRequestedAt(Instant.now());
        user.setStatus(UserStatus.PENDING_DELETION);
        userRepository.save(user);

        // Schedule deletion job
        kafkaTemplate.send("gdpr.events", AccountDeletionScheduledEvent.builder()
            .userId(userId)
            .scheduledAt(scheduledDeletion)
            .reason(reason)
            .build()
        );

        return DeletionResult.builder()
            .scheduledDeletionDate(scheduledDeletion)
            .cancellationToken(generateCancellationToken(userId))
            .build();
    }

    /**
     * Execute account deletion — called by scheduled job after 30-day grace period.
     * Anonymizes data instead of deleting where legally required to retain (e.g., orders).
     */
    @Transactional
    public void executeAccountDeletion(UUID userId) {
        log.info("Executing GDPR deletion for user: {}", userId);

        // 1. Anonymize personal data (keep aggregate data for analytics)
        userRepository.anonymizeUser(userId);

        // 2. Delete media files from S3
        mediaRepository.findAllByUploaderId(userId).forEach(media ->
            s3Service.delete(media.getOriginalUrl())
        );

        // 3. Remove social posts and interactions
        socialPostRepository.deleteAllByAuthorId(userId);
        followRepository.deleteAllByFollowerOrFollowing(userId);

        // 4. Anonymize order data (keep for legal compliance)
        orderRepository.anonymizeOrdersForUser(userId);

        // 5. Remove from AI training data
        aiProfileRepository.deleteByUserId(userId);

        // 6. Mark user as deleted
        userRepository.markAsDeleted(userId);

        log.info("GDPR deletion completed for user: {}", userId);
    }

    /**
     * RIGHT TO RECTIFICATION (Art. 16 GDPR)
     * Already handled by the standard profile update endpoint.
     */

    /**
     * RIGHT TO DATA PORTABILITY (Art. 20 GDPR)
     * Same as exportUserData but in structured, commonly used format.
     */
}
```

### 5.3 Consent Management

```java
@Entity
public class ConsentRecord {
    private UUID id;
    private UUID userId;
    private String consentType;       // GDPR_TERMS, MARKETING, ANALYTICS, PROFILING
    private boolean granted;
    private String version;           // version of terms/policy consented to
    private String ipAddress;         // for audit trail
    private String userAgent;
    private Instant consentedAt;
    private Instant revokedAt;
}

// Privacy policy version tracking
@Configuration
public class PrivacyPolicyConfig {
    @Value("${cerex.gdpr.privacy-policy-version}")
    public static final String CURRENT_VERSION = "2026.03.1";

    @Value("${cerex.gdpr.terms-version}")
    public static final String TERMS_VERSION = "2026.03.1";
}
```

---

## 6. API Security

### 6.1 Rate Limiting Implementation

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimitConfig rateLimitConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clientId = extractClientId(request);
        String endpoint = extractEndpointCategory(request);
        RateLimitPolicy policy = rateLimitConfig.getPolicyFor(clientId, endpoint);

        String key = "ratelimit:" + clientId + ":" + endpoint;
        long currentCount = incrementCounter(key, policy.getWindowSeconds());

        // Set rate limit headers
        response.setHeader("X-RateLimit-Limit", String.valueOf(policy.getMaxRequests()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(
            Math.max(0, policy.getMaxRequests() - currentCount)
        ));
        response.setHeader("X-RateLimit-Reset", String.valueOf(
            System.currentTimeMillis() / 1000 + policy.getWindowSeconds()
        ));

        if (currentCount > policy.getMaxRequests()) {
            log.warn("Rate limit exceeded for client: {} on endpoint: {}", clientId, endpoint);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(policy.getWindowSeconds()));
            response.getWriter().write("{\"error\":{\"code\":\"RATE_LIMIT_EXCEEDED\"}}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private long incrementCounter(String key, long windowSeconds) {
        return redisTemplate.execute(new SessionCallback<Long>() {
            @Override
            public Long execute(RedisOperations ops) throws DataAccessException {
                ops.multi();
                ops.opsForValue().increment(key);
                ops.expire(key, Duration.ofSeconds(windowSeconds));
                List<Object> results = ops.exec();
                return (Long) results.get(0);
            }
        });
    }
}
```

### 6.2 Input Validation

```java
// Custom validators
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoScriptTagValidator.class)
public @interface NoScriptTag {
    String message() default "HTML script tags are not allowed";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

@Component
public class NoScriptTagValidator implements ConstraintValidator<NoScriptTag, String> {
    private static final Pattern SCRIPT_PATTERN =
        Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        return value == null || !SCRIPT_PATTERN.matcher(value).find();
    }
}

// Request DTO with validation
@Data
public class CreateRecipeRequest {
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 300)
    @NoScriptTag
    private String title;

    @NotBlank
    @Size(min = 10, max = 5000)
    @NoScriptTag
    private String description;

    @NotNull
    @Valid
    private List<@Valid IngredientLineRequest> ingredients;

    @NotNull
    @Size(min = 1, max = 50)
    private List<@Valid RecipeStepRequest> steps;
}
```

### 6.3 SQL Injection Prevention

```java
// ALWAYS use parameterized queries — never string concatenation
// Spring Data JPA handles this automatically for derived queries.
// For native queries, always use @Param:

@Query(value = "SELECT * FROM recipes WHERE title ILIKE :searchTerm", nativeQuery = true)
List<Recipe> searchByTitle(@Param("searchTerm") String searchTerm);
// ✓ SAFE: searchTerm is parameterized

// NEVER do this:
// entityManager.createNativeQuery("SELECT * FROM recipes WHERE title LIKE '" + query + "'")
// ✗ UNSAFE: SQL injection vulnerability
```

---

## 7. Fraud Detection for Restaurants

### 7.1 Fraud Signal Collection

```java
// ─────────────────────────────────────────────────────────────
// RestaurantFraudDetectionService.java
// ML-based fraud detection for restaurant partners
// ─────────────────────────────────────────────────────────────
@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantFraudDetectionService {

    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final MLModelClient mlClient;
    private final AlertService alertService;

    /**
     * Calculate fraud risk score for a restaurant (0.0 = clean, 1.0 = high risk).
     * Called: on restaurant approval, weekly batch, and on anomaly triggers.
     */
    public FraudAssessment assessRestaurant(UUID restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow();

        // Feature engineering
        FraudFeatureVector features = FraudFeatureVector.builder()
            // Order pattern features
            .avgOrderValue(orderRepository.getAvgOrderValue(restaurantId))
            .orderVelocity7d(orderRepository.countOrdersInLast7Days(restaurantId))
            .cancellationRate(orderRepository.getCancellationRate(restaurantId))
            .refundRate(orderRepository.getRefundRate(restaurantId))

            // Review pattern features
            .avgRating(restaurant.getAvgRating())
            .ratingStdDev(reviewRepository.getRatingStdDev(restaurantId))
            .reviewVelocity7d(reviewRepository.countReviewsInLast7Days(restaurantId))
            .suspiciousReviewPatternScore(detectReviewManipulation(restaurantId))

            // Account features
            .accountAgeDays(ChronoUnit.DAYS.between(restaurant.getCreatedAt(), Instant.now()))
            .isVerified(restaurant.isVerified())
            .hasHealthCert(restaurant.getHealthCertUrl() != null)

            // Geographic features
            .deliveryDistanceAnomalyScore(calculateDeliveryDistanceAnomaly(restaurantId))

            .build();

        // Call ML model (Isolation Forest + XGBoost ensemble)
        FraudPrediction prediction = mlClient.predict("restaurant_fraud_v2", features);

        // Log fraud signals
        List<String> flags = analyzeFraudFlags(features, prediction);

        // Auto-actions based on score
        if (prediction.getScore() >= 0.9) {
            log.warn("HIGH FRAUD RISK detected for restaurant: {}", restaurantId);
            alertService.sendCriticalAlert("fraud_detection", restaurantId, prediction.getScore());
            // Auto-suspend pending manual review
            restaurantRepository.setStatus(restaurantId, RestaurantStatus.SUSPENDED);
        } else if (prediction.getScore() >= 0.7) {
            log.warn("MEDIUM FRAUD RISK for restaurant: {}", restaurantId);
            alertService.sendAlert("fraud_detection", restaurantId, prediction.getScore());
            restaurantRepository.setManualReviewRequired(restaurantId, true);
        }

        return FraudAssessment.builder()
            .restaurantId(restaurantId)
            .fraudScore(prediction.getScore())
            .flags(flags)
            .assessedAt(Instant.now())
            .requiresManualReview(prediction.getScore() >= 0.7)
            .build();
    }

    private double detectReviewManipulation(UUID restaurantId) {
        // Detect suspicious review patterns:
        // - Sudden burst of 5-star reviews
        // - Reviews from accounts created same day
        // - Reviews with identical text patterns
        // - Reviews from same IP range
        ReviewPatternAnalysis analysis = reviewRepository.analyzeReviewPatterns(restaurantId);

        double suspicionScore = 0.0;
        if (analysis.getNewAccountReviewRatio() > 0.3) suspicionScore += 0.3;
        if (analysis.getBurstDetected()) suspicionScore += 0.4;
        if (analysis.getDuplicateTextRatio() > 0.1) suspicionScore += 0.3;

        return Math.min(suspicionScore, 1.0);
    }
}
```

---

## 8. Data Retention Policies

### 8.1 Retention Schedule

| Data Category | Retention Period | Justification |
|--------------|-----------------|---------------|
| User account data | Account lifetime + 30 days | Grace period |
| Order history | 7 years | French tax law (CGI art. L123-22) |
| Payment records | 7 years | PCI DSS + tax compliance |
| Access logs | 12 months | Security monitoring |
| Audit logs | 5 years | Regulatory compliance |
| Recipe content (published) | Indefinitely | Platform content |
| Draft recipes | 90 days (if unpublished) | Storage optimization |
| Notification data | 90 days | Operational needs |
| Analytics events | 13 months | GDPR cookie directive |
| AI training data | 24 months | Model performance |
| Fraud detection data | 5 years | Risk management |
| Support tickets | 3 years | Customer service |
| Marketing consent | Until revoked | GDPR compliance |

### 8.2 Automated Data Cleanup

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class DataRetentionScheduler {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final NotificationRepository notificationRepository;
    private final DraftRecipeRepository draftRepository;

    // Runs daily at 2:00 AM UTC
    @Scheduled(cron = "0 0 2 * * *", zone = "UTC")
    @DistributedLock(key = "data_retention_cleanup", ttl = 3600)
    public void executeRetentionPolicies() {
        log.info("Starting data retention cleanup...");

        // Execute pending GDPR deletions
        int gdprDeletions = userRepository
            .findPendingDeletionsBefore(Instant.now())
            .forEach(userId -> gdprService.executeAccountDeletion(userId))
            .size();

        // Delete old notifications
        int notifDeleted = notificationRepository
            .deleteOlderThan(Instant.now().minus(Duration.ofDays(90)));

        // Archive old draft recipes
        int draftsArchived = draftRepository
            .archiveUnpublishedOlderThan(Instant.now().minus(Duration.ofDays(90)));

        // Anonymize old analytics events
        int analyticsAnonymized = analyticsRepository
            .anonymizeOlderThan(Instant.now().minus(Duration.ofDays(395)));

        log.info("Retention cleanup complete: {} GDPR deletions, {} notifications removed, " +
                 "{} drafts archived, {} analytics anonymized",
            gdprDeletions, notifDeleted, draftsArchived, analyticsAnonymized);

        // Report to compliance dashboard
        complianceService.recordRetentionExecution(RetentionReport.builder()
            .executedAt(Instant.now())
            .gdprDeletions(gdprDeletions)
            .notificationsDeleted(notifDeleted)
            .draftsArchived(draftsArchived)
            .build());
    }
}
```

---

## 9. Security Monitoring

### 9.1 Audit Log

```java
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityAuditAspect {

    private final AuditLogRepository auditRepo;

    @Around("@annotation(audited)")
    public Object auditAction(ProceedingJoinPoint pjp, Audited audited) throws Throwable {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = extractUserId(auth);
        String action = audited.action();
        Instant startTime = Instant.now();

        try {
            Object result = pjp.proceed();
            auditRepo.save(AuditLog.success(userId, action, pjp.getArgs()));
            return result;
        } catch (Exception e) {
            auditRepo.save(AuditLog.failure(userId, action, e.getMessage()));
            throw e;
        }
    }
}

// Alerting for suspicious activity
@Component
@RequiredArgsConstructor
public class SecurityEventMonitor {

    @EventListener
    public void onFailedLoginAttempt(FailedLoginEvent event) {
        // Alert if 5+ failed logins in 15 minutes from same IP
        if (failedLoginCount(event.getIp(), Duration.ofMinutes(15)) >= 5) {
            alertService.sendAlert("BRUTE_FORCE_DETECTED", event.getIp());
            ipBlocklistService.blockTemporarily(event.getIp(), Duration.ofHours(1));
        }
    }
}
```

---

## 10. Incident Response Plan

### 10.1 Security Incident Classification

| Severity | Description | Response Time | Escalation |
|----------|-------------|--------------|------------|
| P0 Critical | Data breach, system compromise | 15 minutes | CEO, CTO, Legal |
| P1 High | Auth bypass, privilege escalation | 1 hour | CTO, Security team |
| P2 Medium | DDoS, rate limit bypass | 4 hours | Dev team lead |
| P3 Low | Vulnerability discovery | 24 hours | Dev team |

### 10.2 Data Breach Response (GDPR Art. 33/34)

```
Within 72 hours of discovery:
1. Notify supervisory authority (CNIL in France)
   - Nature of the breach
   - Categories and approximate number of affected individuals
   - Likely consequences
   - Measures taken

2. If high risk to individuals:
   - Notify affected users without undue delay
   - Provide clear information on breach and protective actions

3. Document:
   - Timeline of events
   - Data affected
   - Remediation steps
   - Lessons learned
```

---

*Document Version: 1.0.0 | Classification: CONFIDENTIAL | Owner: Cerex Security & Compliance Team*
*Next Review: 2026-09-30 | Reviewed by DPO: [Pending] | Legal Review: [Pending]*
