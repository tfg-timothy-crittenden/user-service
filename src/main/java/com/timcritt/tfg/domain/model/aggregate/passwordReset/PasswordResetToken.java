package com.timcritt.tfg.domain.model.aggregate.passwordReset;

import java.time.Duration;
import java.time.Instant;

public class PasswordResetToken {

    private final Long id;
    private final Long userId;
    private final String tokenHash;
    private final Instant createdAt;
    private final Instant expiresAt;
    private boolean valid = true;

    private PasswordResetToken(Long id, Long userId, String tokenHash, Instant createdAt, Instant expiresAt, boolean valid) {

        if(userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }

        requireNonBlank(tokenHash, "tokenHash");
        requireNotNull(createdAt, "createdAt cannot be null");
        requireNotNull(expiresAt, "expiresAt cannot be null");

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
    public static PasswordResetToken create(Long userId, String tokenHash, Instant createdAt) {

        requireNotNull(createdAt, "createdAt cannot be null");

        Instant expiresAt = createdAt.plus(Duration.ofHours(1));
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

    //Pass the time in to facilitate testing and remove the dependency. Current time should not belong to this class.
    public boolean isExpiredAt(Instant now) {
        requireNotNull(now, "now cannot be null");

        return !now.isBefore(expiresAt);
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

    private static void requireNotNull(Object object, String fieldName) {
        if (object == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
    }

}
