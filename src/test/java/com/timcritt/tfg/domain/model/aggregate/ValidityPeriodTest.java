package com.timcritt.tfg.domain.model.aggregate;

import com.timcritt.tfg.domain.model.ValidityPeriod;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ValidityPeriodTest {

    private static final Instant CREATED_AT =
            Instant.parse("2026-09-15T12:00:00Z");

    @Test
    void betweenShouldCreateValidityPeriod() {
        Instant expiresAt = CREATED_AT.plus(Duration.ofHours(1));

        ValidityPeriod validityPeriod =
                ValidityPeriod.between(CREATED_AT, expiresAt);

        assertEquals(CREATED_AT, validityPeriod.createdAt());
        assertEquals(expiresAt, validityPeriod.expiresAt());
    }

    @Test
    void betweenShouldThrowWhenCreatedAtIsNull() {
        Instant expiresAt = CREATED_AT.plus(Duration.ofHours(1));

        assertThrows(
                IllegalArgumentException.class,
                () -> ValidityPeriod.between(null, expiresAt)
        );
    }

    @Test
    void betweenShouldThrowWhenExpiresAtIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ValidityPeriod.between(CREATED_AT, null)
        );
    }

    @Test
    void betweenShouldThrowWhenExpiresAtIsBeforeCreatedAt() {
        Instant expiresAt = CREATED_AT.minus(Duration.ofSeconds(1));

        assertThrows(
                IllegalArgumentException.class,
                () -> ValidityPeriod.between(CREATED_AT, expiresAt)
        );
    }

    @Test
    void betweenShouldThrowWhenExpiresAtEqualsCreatedAt() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ValidityPeriod.between(CREATED_AT, CREATED_AT)
        );
    }

    @Test
    void startingAtShouldCreatePeriodUsingProvidedDuration() {
        Duration duration = Duration.ofDays(1);

        ValidityPeriod validityPeriod =
                ValidityPeriod.startingAt(CREATED_AT, duration);

        assertEquals(CREATED_AT, validityPeriod.createdAt());
        assertEquals(
                CREATED_AT.plus(duration),
                validityPeriod.expiresAt()
        );
    }

    @Test
    void startingAtShouldThrowWhenCreatedAtIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ValidityPeriod.startingAt(
                        null,
                        Duration.ofHours(1)
                )
        );
    }

    @Test
    void startingAtShouldThrowWhenDurationIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ValidityPeriod.startingAt(
                        CREATED_AT,
                        null
                )
        );
    }

    @Test
    void startingAtShouldThrowWhenDurationIsZero() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ValidityPeriod.startingAt(
                        CREATED_AT,
                        Duration.ZERO
                )
        );
    }

    @Test
    void startingAtShouldThrowWhenDurationIsNegative() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ValidityPeriod.startingAt(
                        CREATED_AT,
                        Duration.ofHours(-1)
                )
        );
    }

    @Test
    void isExpiredAtShouldReturnFalseBeforeExpiry() {
        ValidityPeriod validityPeriod =
                ValidityPeriod.startingAt(
                        CREATED_AT,
                        Duration.ofHours(1)
                );

        Instant beforeExpiry =
                CREATED_AT.plus(Duration.ofMinutes(59));

        assertFalse(
                validityPeriod.isExpiredAt(beforeExpiry)
        );
    }

    @Test
    void isExpiredAtShouldReturnTrueExactlyAtExpiry() {
        Instant expiresAt =
                CREATED_AT.plus(Duration.ofHours(1));

        ValidityPeriod validityPeriod =
                ValidityPeriod.between(
                        CREATED_AT,
                        expiresAt
                );

        assertTrue(
                validityPeriod.isExpiredAt(expiresAt)
        );
    }

    @Test
    void isExpiredAtShouldReturnTrueAfterExpiry() {
        ValidityPeriod validityPeriod =
                ValidityPeriod.startingAt(
                        CREATED_AT,
                        Duration.ofHours(1)
                );

        Instant afterExpiry =
                CREATED_AT.plus(Duration.ofHours(2));

        assertTrue(
                validityPeriod.isExpiredAt(afterExpiry)
        );
    }

    @Test
    void isExpiredAtShouldThrowWhenNowIsNull() {
        ValidityPeriod validityPeriod =
                ValidityPeriod.startingAt(
                        CREATED_AT,
                        Duration.ofHours(1)
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> validityPeriod.isExpiredAt(null)
        );
    }

    @Test
    void equalValidityPeriodsShouldBeEqual() {
        Instant expiresAt =
                CREATED_AT.plus(Duration.ofHours(1));

        ValidityPeriod first =
                ValidityPeriod.between(
                        CREATED_AT,
                        expiresAt
                );

        ValidityPeriod second =
                ValidityPeriod.between(
                        CREATED_AT,
                        expiresAt
                );

        assertEquals(first, second);
    }

    @Test
    void equalValidityPeriodsShouldHaveSameHashCode() {
        Instant expiresAt =
                CREATED_AT.plus(Duration.ofHours(1));

        ValidityPeriod first =
                ValidityPeriod.between(
                        CREATED_AT,
                        expiresAt
                );

        ValidityPeriod second =
                ValidityPeriod.between(
                        CREATED_AT,
                        expiresAt
                );

        assertEquals(
                first.hashCode(),
                second.hashCode()
        );
    }

    @Test
    void validityPeriodsWithDifferentCreatedAtShouldNotBeEqual() {
        ValidityPeriod first =
                ValidityPeriod.between(
                        CREATED_AT,
                        CREATED_AT.plus(Duration.ofHours(1))
                );

        ValidityPeriod second =
                ValidityPeriod.between(
                        CREATED_AT.plus(Duration.ofMinutes(1)),
                        CREATED_AT.plus(Duration.ofHours(1))
                );

        assertNotEquals(first, second);
    }

    @Test
    void validityPeriodsWithDifferentExpiresAtShouldNotBeEqual() {
        ValidityPeriod first =
                ValidityPeriod.between(
                        CREATED_AT,
                        CREATED_AT.plus(Duration.ofHours(1))
                );

        ValidityPeriod second =
                ValidityPeriod.between(
                        CREATED_AT,
                        CREATED_AT.plus(Duration.ofHours(2))
                );

        assertNotEquals(first, second);
    }

    @Test
    void validityPeriodShouldEqualItself() {
        ValidityPeriod validityPeriod =
                ValidityPeriod.startingAt(
                        CREATED_AT,
                        Duration.ofHours(1)
                );

        assertEquals(validityPeriod, validityPeriod);
    }

    @Test
    void validityPeriodShouldNotEqualNull() {
        ValidityPeriod validityPeriod =
                ValidityPeriod.startingAt(
                        CREATED_AT,
                        Duration.ofHours(1)
                );

        assertNotEquals(null, validityPeriod);
    }

    @Test
    void validityPeriodShouldNotEqualDifferentClass() {
        ValidityPeriod validityPeriod =
                ValidityPeriod.startingAt(
                        CREATED_AT,
                        Duration.ofHours(1)
                );

        assertNotEquals(
                validityPeriod,
                "not a validity period"
        );
    }
}
