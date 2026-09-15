
package com.timcritt.tfg.domain.model.aggregate.user;

import com.timcritt.tfg.domain.model.ValidityPeriod;
import com.timcritt.tfg.domain.model.aggregate.user.EmailVerificationStatus;

import java.time.Duration;
import java.time.Instant;

public class EmailVerificationToken {

    private static final Duration VALIDITY_DURATION = Duration.ofDays(1);

    private final Long id;
    private final Long userId;
    private final String userEmail;
    private final String token;
    private final ValidityPeriod validityPeriod;

    private Instant confirmedAt;
    private EmailVerificationStatus status;

    private EmailVerificationToken(
            Long id,
            Long userId,
            String userEmail,
            String token,
            ValidityPeriod validityPeriod,
            Instant confirmedAt,
            EmailVerificationStatus status
    ) {
        requireNotNull(userId, "userId");
        requireNonBlank(userEmail, "userEmail");
        requireNonBlank(token, "token");
        requireNotNull(validityPeriod, "validityPeriod");
        requireNotNull(status, "status");

        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.token = token;
        this.validityPeriod = validityPeriod;
        this.confirmedAt = confirmedAt;
        this.status = status;
    }

    //############################################ Static Factory Methods #############################################

    public static EmailVerificationToken create(
            Long userId,
            String userEmail,
            String token,
            Instant createdAt
    ) {
        return new EmailVerificationToken(
                null,
                userId,
                userEmail,
                token,
                ValidityPeriod.startingAt(
                        createdAt,
                        VALIDITY_DURATION
                ),
                null,
                EmailVerificationStatus.PENDING
        );
    }

    public static EmailVerificationToken rehydrate(
            Long id,
            Long userId,
            String userEmail,
            String token,
            Instant createdAt,
            Instant expiresAt,
            Instant confirmedAt,
            EmailVerificationStatus status
    ) {
        requireNotNull(id, "id");

        return new EmailVerificationToken(
                id,
                userId,
                userEmail,
                token,
                ValidityPeriod.between(
                        createdAt,
                        expiresAt
                ),
                confirmedAt,
                status
        );
    }

    //############################################ Getters ##################################################

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getToken() {
        return token;
    }

    public Instant getCreatedAt() {
        return validityPeriod.createdAt();
    }

    public Instant getExpiresAt() {
        return validityPeriod.expiresAt();
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public EmailVerificationStatus getStatus() {
        return status;
    }

    //############################################ Business Logic ##################################################

    public boolean isExpiredAt(Instant now) {
        return validityPeriod.isExpiredAt(now);
    }

    public void confirmAt(Instant now) {
        if (isExpiredAt(now)) {
            throw new IllegalStateException("Token is expired");
        }

        if (status != EmailVerificationStatus.PENDING) {
            throw new IllegalStateException(
                    "Can only confirm a pending verification token"
            );
        }

        status = EmailVerificationStatus.CONFIRMED;
        confirmedAt = now;
    }

    public void cancelAt(Instant now) {
        if (isExpiredAt(now)) {
            throw new IllegalStateException("Token is expired");
        }

        if (status != EmailVerificationStatus.PENDING) {
            throw new IllegalStateException(
                    "Can only cancel a pending verification token"
            );
        }

        status = EmailVerificationStatus.CANCELLED;
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
            Object value,
            String fieldName
    ) {
        if (value == null) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be null"
            );
        }
    }
}

