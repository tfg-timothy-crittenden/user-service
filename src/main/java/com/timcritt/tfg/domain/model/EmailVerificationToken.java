package com.timcritt.tfg.domain.model;

import java.time.Instant;

public class EmailVerificationToken {

    private Long id;
    private Long userId;
    private String userEmail;
    private String token;
    private Instant createdAt;
    private Instant expiresAt;
    private Instant confirmedAt;
    private EmailVerificationStatus status;

    public EmailVerificationToken() {
    }

    public EmailVerificationToken(Long id, Long userId, String userEmail, String token, Instant createdAt, Instant expiresAt) {
        if (expiresAt == null) {
            throw new IllegalArgumentException("expiresAt cannot be null");
        }
        if (createdAt != null && !expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException("expiresAt must be after createdAt");
        }

        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.token = token;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.status = EmailVerificationStatus.PENDING;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getUserEmail() { return userEmail; }
    public String getToken() { return token; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getConfirmedAt() { return confirmedAt; }
    public EmailVerificationStatus getStatus() { return status; }
    public boolean isExpired() { return expiresAt != null && expiresAt.isBefore(Instant.now()); }

    public void confirm() {
        if (this.isExpired()) {
            throw new IllegalStateException("Token is expired");
        }
        if (status != EmailVerificationStatus.PENDING) {
            throw new IllegalStateException("Can only confirm a pending verification token");
        }
        status = EmailVerificationStatus.CONFIRMED;
        this.confirmedAt = Instant.now();
    }

    public void cancel() {
        if (this.isExpired()) {
            throw new IllegalStateException("Token is expired");
        }
        if (status != EmailVerificationStatus.PENDING) {
            throw new IllegalStateException("Can only cancel a pending verification token");
        }
        status = EmailVerificationStatus.CANCELLED;
    }
}
