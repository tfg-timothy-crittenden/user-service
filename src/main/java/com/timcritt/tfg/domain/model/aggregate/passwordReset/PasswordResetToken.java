
package com.timcritt.tfg.domain.model.aggregate.passwordReset;

import com.timcritt.tfg.domain.exception.InvalidPasswordResetTokenException;
import com.timcritt.tfg.domain.model.ValidityPeriod;

import java.time.Duration;
import java.time.Instant;

public class PasswordResetToken {

    private static final Duration VALIDITY_DURATION = Duration.ofHours(1);

    private final Long id;
    private final Long userId;
    private final String tokenHash;
    private final ValidityPeriod validityPeriod;
    private boolean valid;

    private PasswordResetToken(
            Long id,
            Long userId,
            String tokenHash,
            ValidityPeriod validityPeriod,
            boolean valid
    ) {
        requireNotNull(userId, "userId");
        requireNonBlank(tokenHash, "tokenHash");
        requireNotNull(validityPeriod, "validityPeriod");

        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.validityPeriod = validityPeriod;
        this.valid = valid;
    }

    //############################################ Static Factory Methods #############################################

    public static PasswordResetToken create(
            Long userId,
            String tokenHash,
            Instant createdAt
    ) {
        return new PasswordResetToken(
                null,
                userId,
                tokenHash,
                ValidityPeriod.startingAt(
                        createdAt,
                        VALIDITY_DURATION
                ),
                true
        );
    }

    public static PasswordResetToken rehydrate(
            Long id,
            Long userId,
            String tokenHash,
            Instant createdAt,
            Instant expiresAt,
            boolean valid
    ) {
        requireNotNull(id, "id");

        return new PasswordResetToken(
                id,
                userId,
                tokenHash,
                ValidityPeriod.between(
                        createdAt,
                        expiresAt
                ),
                valid
        );
    }

    //############################################ Getters ##################################################

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
        return validityPeriod.createdAt();
    }

    public Instant getExpiresAt() {
        return validityPeriod.expiresAt();
    }

    public boolean isValid() {
        return valid;
    }

    //############################################ Business Logic ##################################################

    public boolean isExpiredAt(Instant now) {
        return validityPeriod.isExpiredAt(now);
    }

    public boolean isUsableAt(Instant now) {
        return valid && !validityPeriod.isExpiredAt(now);
    }

    public void consumeAt(Instant now) {
        if (!isUsableAt(now)) {
            throw new InvalidPasswordResetTokenException();
        }

        valid = false;
    }

    public void revoke() {
        valid = false;
    }

    //############################################ Helpers ##################################################

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

    private static void requireNotNull(
            Object object,
            String fieldName
    ) {
        if (object == null) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be null"
            );
        }
    }
}

