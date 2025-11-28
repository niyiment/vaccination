package com.niyiment.authservice.services.security;

import com.niyiment.authservice.domain.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * JTW token provider for generating and validating JWT tokens.
 * Uses HMAC SHA-256 for signing and verification.
 */
@Slf4j
@Component
public class JwtTokenProvider {
    private final SecretKey secretKey;
    private final long jwtExpirationInMs;
    private final long refreshExpirationMs;
    private  final String issuer;

    public JwtTokenProvider(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.expiration-ms}") long jwtExpirationInMs,
            @Value("${app.security.jwt.refresh-expiration-ms}") long refreshExpirationMs,
            @Value("${app.security.jwt.issuer}") String issuer
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationInMs = jwtExpirationInMs;
        this.refreshExpirationMs = refreshExpirationMs;
        this.issuer = issuer;
    }

    /**
     * Generate access token for the authenticated user.
     */
    public String generateAccessToken(User user, Collection<? extends GrantedAuthority> authorities) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());
        claims.put("roles", authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList())
        );

        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(jwtExpirationInMs);

        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Generate refresh token for the authenticated user.
     */
    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(refreshExpirationMs);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .claim("tokenType", "refresh")
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Extract username from the JWT token.
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);

        return claims.getSubject();
    }

    /**
     * Extract user ID from JWT token
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);

        return claims.get("userId", Long.class);
    }

    /**
     * Validate JWT token.
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Get expiration time for the JWT token.
     */
    public Instant getExpirationFromToken(String token) {
        Claims claims = parseToken(token);

        return claims.getExpiration().toInstant();
    }

    /**
     * Check if the JWT token is expired.
     */
    public boolean isTokenExpired(String token) {
        try {
            Instant expiration = getExpirationFromToken(token);
            return expiration.isBefore(Instant.now());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * Get refresh token expiration time.
     */
    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    /**
     * Parse JWT token and return claims.
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
