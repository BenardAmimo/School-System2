package com.school.security.service;

import com.school.security.entity.UserReg;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {

    // No default value on purpose: the app must not start without a real secret.
    // Generate one with:  openssl rand -base64 48   and supply it as an environment variable.
    @Value("${school.security.jwt-secret}")
    private String secretKey;

    // Milliseconds.
    @Value("${school.security.jwt-expiration}")
    private long jwtExpiration;

    private Key signingKey;

    /** Build the key once and fail at startup, not on the first login. */
    @PostConstruct
    void init() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretKey);
        } catch (RuntimeException e) {
            throw new IllegalStateException("school.security.jwt-secret must be valid Base64", e);
        }
        if (keyBytes.length < 32) {
            throw new IllegalStateException("school.security.jwt-secret must decode to at least 32 bytes (256 bits)");
        }
        if (jwtExpiration <= 0) {
            throw new IllegalStateException("school.security.jwt-expiration must be a positive number of milliseconds");
        }
        if (jwtExpiration > 24L * 60 * 60 * 1000) {
            log.warn("JWT lifetime is over 24 hours. A stolen token stays usable that long; there is no revocation.");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        UserReg user = (UserReg) userDetails;
        // Informational only (for the frontend). Authorization always uses the role loaded from the database.
        claims.put("role", user.getRole().name());
        return generateToken(claims, userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(extraClaims)               // must come first: it replaces all claims
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + jwtExpiration))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * parseClaimsJws verifies the signature and rejects expired tokens (it throws), so an exception here
     * means "not valid". JwtAuthFilter catches it and treats the request as unauthenticated.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username != null && username.equals(userDetails.getUsername());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }
}