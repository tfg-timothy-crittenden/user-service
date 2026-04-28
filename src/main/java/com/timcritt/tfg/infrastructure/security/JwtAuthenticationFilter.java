package com.timcritt.tfg.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final SecretKey secretKey;

    public JwtAuthenticationFilter(UserDetailsService userDetailsService, String secret) {
        this.userDetailsService = userDetailsService;
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes.length >= 32 ? keyBytes : padTo32Bytes(keyBytes));
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (claims.getExpiration() != null && claims.getExpiration().toInstant().isBefore(Instant.now())) {
                filterChain.doFilter(request, response);
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                String username = claims.getSubject();

                // Try to read roles from the token. If present, use them to populate authorities and avoid a DB lookup.
                java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities = java.util.Collections.<org.springframework.security.core.GrantedAuthority>emptyList();
                Object rolesObj = claims.get("roles");
                if (rolesObj instanceof java.util.Collection<?> roleList) {
                    java.util.List<org.springframework.security.core.GrantedAuthority> auths = new java.util.ArrayList<>();
                    for (Object r : roleList) {
                        if (r != null) {
                            String roleStr = r.toString();
                            if (!roleStr.startsWith("ROLE_")) {
                                roleStr = "ROLE_" + roleStr;
                            }
                            auths.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(roleStr));
                        }
                    }
                    authorities = auths;
                } else {
                    // Fallback: load authorities from UserDetailsService
                    try {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        authorities = userDetails.getAuthorities();
                    } catch (Exception ignored) {
                        // Leave authorities as the empty list assigned earlier
                    }
                }

                // Build a CustomUserPrincipal from token claims so principal carries the id and authorities
                Long userId = null;
                Object idObj = claims.get("userId");
                if (idObj instanceof Number) {
                    userId = ((Number) idObj).longValue();
                }

                com.timcritt.tfg.infrastructure.security.CustomUserPrincipal principal =
                        new com.timcritt.tfg.infrastructure.security.CustomUserPrincipal(userId, username, null, true, authorities);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        authorities
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ignored) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private byte[] padTo32Bytes(byte[] input) {
        byte[] padded = new byte[32];
        System.arraycopy(input, 0, padded, 0, Math.min(input.length, padded.length));
        return padded;
    }
}
