package com.timcritt.tfg.domain.model.aggregate.passwordReset;

import java.time.Duration;
import java.time.Instant;

public class PasswordResetToken {

    private Long id;
    private Long userId;
    private String tokenHash;
    private Instant createdAt;
    private Instant expiresAt;
    private boolean valid = true;


    private PasswordResetToken(Long id, Long userId, String tokenHash, Instant createdAt, Instant expiresAt, boolean valid) {

        if(userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }

        requireNonBlank(tokenHash, "tokenHash");

        if(createdAt == null) {
            throw new IllegalArgumentException("createdAt cannot be null");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("expiresAt cannot be null");
        }
        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException("expiresAt must be after createdAt");
        }

        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.valid = valid;
    }

    //############################################ Static Factory Methods #############################################
    public static PasswordResetToken create(Long userId, String tokenHash) {
        Instant createdAt = Instant.now();
        Instant expiresAt = Instant.now().plus(Duration.ofHours(1));
        return new PasswordResetToken(null, userId, tokenHash, createdAt, expiresAt, true);
    }

    public static PasswordResetToken rehydrate(Long id, Long userId, String tokenHash, Instant createdAt, Instant expiresAt, boolean valid) {
        return new PasswordResetToken(id, userId, tokenHash, createdAt, expiresAt, valid);
    }

    //############################################ Generic Getters ##################################################
    public Long getId() {
        return id;
    }
    public Long getUserId() {
        return userId;
    }
    public String getTokenHash() {
        return tokenHash;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public Instant getExpiresAt() {
        return expiresAt;
    }
    public boolean isValid() {
        return valid;
    }
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(Instant.now());
    }
    public void invalidate() {
        this.valid = false;
    }

    //########################################## Helpers #########################################################
    private static void requireNonBlank(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be blank"
            );
        }
    }

}
