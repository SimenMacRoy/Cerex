package com.cerex.security;

import com.cerex.config.CerexProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * JWT token generation and validation service.
 *
 * <p>Generates HS256 signed tokens with userId as subject.
 * Access tokens: 1h TTL. Refresh tokens: 7d TTL.
 */
@Service
@Slf4j
public class JwtService {

    private final SecretKey signingKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final String issuer;

    public JwtService(CerexProperties cerexProperties) {
        byte[] keyBytes = Decoders.BASE64.decode(
            java.util.Base64.getEncoder().encodeToString(
                cerexProperties.getJwt().getSecret().getBytes()
            )
        );
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpiration = cerexProperties.getJwt().getAccessTokenExpiration();
        this.refreshTokenExpiration = cerexProperties.getJwt().getRefreshTokenExpiration();
        this.issuer = cerexProperties.getJwt().getIssuer();
    }

    /**
     * Generate an access token for the authenticated user.
     */
    public String generateAccessToken(Authentication authentication) {
        CerexUserDetails userDetails = (CerexUserDetails) authentication.getPrincipal();
        return generateAccessToken(userDetails);
    }

    public String generateAccessToken(CerexUserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", userDetails.getRole());
        claims.put("email", userDetails.getEmail());
        claims.put("jti", UUID.randomUUID().toString());

        return Jwts.builder()
            .claims(claims)
            .subject(userDetails.getUserId().toString())
            .issuer(issuer)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
            .signWith(signingKey)
            .compact();
    }

    /**
     * Generate a refresh token.
     */
    public String generateRefreshToken(CerexUserDetails userDetails) {
        return Jwts.builder()
            .subject(userDetails.getUserId().toString())
            .claim("jti", UUID.randomUUID().toString())
            .claim("type", "refresh")
            .issuer(issuer)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
            .signWith(signingKey)
            .compact();
    }

    /**
     * Extract the user ID (subject) from a token.
     */
    public UUID extractUserId(String token) {
        return UUID.fromString(extractClaim(token, Claims::getSubject));
    }

    /**
     * Extract the user email from a token.
     */
    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    /**
     * Validate token against a UserDetails object.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final UUID userId = extractUserId(token);
            CerexUserDetails cerexUser = (CerexUserDetails) userDetails;
            return userId.equals(cerexUser.getUserId()) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
