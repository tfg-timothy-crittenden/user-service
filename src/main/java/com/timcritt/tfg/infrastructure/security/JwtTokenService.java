package com.timcritt.tfg.infrastructure.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JwtTokenService {

    private final SecretKey secretKey;
    private final SignatureAlgorithm algorithm;

    public JwtTokenService(
            @Value("${app.jwt.secret:change-me-change-me-change-me-change-me}") String secret,
            @Value("${app.jwt.algorithm:HS256}") String algorithm
    ) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes.length >= 32 ? keyBytes : padTo32Bytes(keyBytes));
        this.algorithm = SignatureAlgorithm.valueOf(algorithm);
    }

    public String generateToken(Long userId, String username, String name, String surname, Set<String> roles) {
        Instant now = Instant.now();

        Set<String> normalizedRoles = java.util.Optional.ofNullable(roles)
                .map(rs -> rs.stream()
                        .filter(java.util.Objects::nonNull)
                        .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                        .collect(Collectors.toSet()))
                .orElse(java.util.Set.of());

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("username", username)
                .claim("name", name)
                .claim("surname", surname)
                .claim("roles", normalizedRoles)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(1, ChronoUnit.HOURS)))
                .signWith(secretKey, algorithm)
                .compact();
    }

    private byte[] padTo32Bytes(byte[] input) {
        byte[] padded = new byte[32];
        System.arraycopy(input, 0, padded, 0, Math.min(input.length, padded.length));
        return padded;
    }
}