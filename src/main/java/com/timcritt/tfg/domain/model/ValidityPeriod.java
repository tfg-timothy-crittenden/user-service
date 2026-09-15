package com.timcritt.tfg.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public final class ValidityPeriod {

    private final Instant createdAt;
    private final Instant expiresAt;

    private ValidityPeriod(
            Instant createdAt,
            Instant expiresAt
    ) {
        requireNotNull(createdAt, "createdAt");
        requireNotNull(expiresAt, "expiresAt");

        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException(
                    "expiresAt must be after createdAt"
            );
        }

        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public static ValidityPeriod between(
            Instant createdAt,
            Instant expiresAt
    ) {
        return new ValidityPeriod(
                createdAt,
                expiresAt
        );
    }

    public static ValidityPeriod startingAt(
            Instant createdAt,
            Duration duration
    ) {
        requireNotNull(createdAt, "createdAt");
        requireNotNull(duration, "duration");

        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException(
                    "duration must be positive"
            );
        }

        return new ValidityPeriod(
                createdAt,
                createdAt.plus(duration)
        );
    }

    public boolean isExpiredAt(Instant now) {
        requireNotNull(now, "now");

        return !now.isBefore(expiresAt);
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant expiresAt() {
        return expiresAt;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ValidityPeriod that = (ValidityPeriod) o;

        return createdAt.equals(that.createdAt)
                && expiresAt.equals(that.expiresAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                createdAt,
                expiresAt
        );
    }
}