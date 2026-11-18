package com.scamshield.security;

import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.scamshield.user.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret-base64}") String secretBase64,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        byte[] secretBytes = Decoders.BASE64.decode(secretBase64);
        if (secretBytes.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET_BASE64 must decode to at least 32 bytes.");
        }
        this.signingKey = Keys.hmacShaKeyFor(secretBytes);
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("role", user.getRole().name())
                .claim("tokenVersion", user.getTokenVersion())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(signingKey)
                .compact();
    }

    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, String email, long expectedTokenVersion) {
        Claims claims = getClaims(token);
        Object versionClaim = claims.get("tokenVersion");
        long tokenVersion = versionClaim instanceof Number number ? number.longValue() : 0L;
        return email.equals(claims.getSubject())
                && claims.getExpiration().after(new Date())
                && tokenVersion == expectedTokenVersion;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
