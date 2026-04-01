package com.timcritt.tfg.domain.model;

import java.time.Instant;

public class PasswordResetToken {

    private Long id;
    private Long userId;
    private String tokenHash;
    private Instant createdAt;
    private Instant expiresAt;
    private boolean valid = true; // NEW: tokens are valid by default

    public PasswordResetToken() {
    }

    // constructor: id, userId, tokenHash, createdAt, expiresAt, valid
    public PasswordResetToken(Long id, Long userId, String tokenHash, Instant createdAt, Instant expiresAt, boolean valid) {
        if (expiresAt == null) {
            throw new IllegalArgumentException("expiresAt cannot be null");
        }
        if (createdAt != null && !expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException("expiresAt must be after createdAt");
        }

        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.valid = valid;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getTokenHash() {
        return tokenHash;
    }
    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    public Instant getExpiresAt() {
        return expiresAt;
    }
    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
    public boolean isValid() {
        return valid;
    }
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(Instant.now());
    }

    public void invalidate() {
        this.valid = false;
    }

}
