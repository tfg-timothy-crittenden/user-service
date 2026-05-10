package com.timcritt.tfg.infrastructure.ratelimiting;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory rate limiter for public/sensitive resend endpoints.
 *
 * <p><b>Resend verification email</b> (public):
 * <ul>
 *   <li>Per email: 1 request per {@code app.rate-limit.resend-verification.email-cooldown-seconds} (default 60 s)</li>
 *   <li>Per IP: max {@code app.rate-limit.resend-verification.ip-max-requests} per
 *       {@code app.rate-limit.resend-verification.ip-window-seconds} (defaults: 5 / 60 s)</li>
 * </ul>
 *
 * <p><b>Resend platform invitation</b> (admin):
 * <ul>
 *   <li>Per invitation ID: 1 request per {@code app.rate-limit.resend-invitation.id-cooldown-seconds} (default 60 s)</li>
 *   <li>Per IP: max {@code app.rate-limit.resend-invitation.ip-max-requests} per
 *       {@code app.rate-limit.resend-invitation.ip-window-seconds} (defaults: 10 / 60 s)</li>
 * </ul>
 *
 * <p>All caches expire entries automatically via Caffeine, so memory stays bounded.
 */
@Component
public class ResendVerificationRateLimiter {

    // --- verification email caches ---
    private final Cache<String, Boolean> emailCooldown;
    private final Cache<String, AtomicInteger> verificationIpCounter;
    private final int verificationIpMaxRequests;

    // --- invitation resend caches ---
    private final Cache<Long, Boolean> invitationIdCooldown;
    private final Cache<String, AtomicInteger> invitationIpCounter;
    private final int invitationIpMaxRequests;

    public ResendVerificationRateLimiter(
            @Value("${app.rate-limit.resend-verification.email-cooldown-seconds:60}") int emailCooldownSeconds,
            @Value("${app.rate-limit.resend-verification.ip-window-seconds:60}") int verificationIpWindowSeconds,
            @Value("${app.rate-limit.resend-verification.ip-max-requests:5}") int verificationIpMaxRequests,
            @Value("${app.rate-limit.resend-invitation.id-cooldown-seconds:60}") int invitationIdCooldownSeconds,
            @Value("${app.rate-limit.resend-invitation.ip-window-seconds:60}") int invitationIpWindowSeconds,
            @Value("${app.rate-limit.resend-invitation.ip-max-requests:10}") int invitationIpMaxRequests) {

        this.verificationIpMaxRequests = verificationIpMaxRequests;
        this.invitationIpMaxRequests = invitationIpMaxRequests;

        this.emailCooldown = Caffeine.newBuilder()
                .expireAfterWrite(emailCooldownSeconds, TimeUnit.SECONDS)
                .maximumSize(10_000)
                .build();

        this.verificationIpCounter = Caffeine.newBuilder()
                .expireAfterWrite(verificationIpWindowSeconds, TimeUnit.SECONDS)
                .maximumSize(10_000)
                .build();

        this.invitationIdCooldown = Caffeine.newBuilder()
                .expireAfterWrite(invitationIdCooldownSeconds, TimeUnit.SECONDS)
                .maximumSize(10_000)
                .build();

        this.invitationIpCounter = Caffeine.newBuilder()
                .expireAfterWrite(invitationIpWindowSeconds, TimeUnit.SECONDS)
                .maximumSize(10_000)
                .build();
    }

    /**
     * Rate-limit check for resend-verification-email.
     * Returns {@code true} if allowed; {@code false} to reject with 429.
     */
    public boolean tryConsume(String email, String ip) {
        if (emailCooldown.getIfPresent(email) != null) {
            return false;
        }
        AtomicInteger count = verificationIpCounter.get(ip, k -> new AtomicInteger(0));
        if (count.incrementAndGet() > verificationIpMaxRequests) {
            return false;
        }
        emailCooldown.put(email, Boolean.TRUE);
        return true;
    }

    /**
     * Rate-limit check for resend platform invitation.
     * Returns {@code true} if allowed; {@code false} to reject with 429.
     */
    public boolean tryConsumeInvitation(Long invitationId, String ip) {
        if (invitationIdCooldown.getIfPresent(invitationId) != null) {
            return false;
        }
        AtomicInteger count = invitationIpCounter.get(ip, k -> new AtomicInteger(0));
        if (count.incrementAndGet() > invitationIpMaxRequests) {
            return false;
        }
        invitationIdCooldown.put(invitationId, Boolean.TRUE);
        return true;
    }
}

