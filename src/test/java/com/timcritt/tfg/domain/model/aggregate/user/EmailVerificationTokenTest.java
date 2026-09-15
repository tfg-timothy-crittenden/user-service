package com.timcritt.tfg.domain.model.aggregate.user;


import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class EmailVerificationTokenTest {

    private static final Instant CREATED_AT =
            Instant.parse("2026-09-15T12:00:00Z");

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "tim@example.com";
    private static final String TOKEN = "verification-token";

    //############################################ Creation ##################################################

    @Test
    void createShouldCreatePendingTokenValidForOneDay() {
        EmailVerificationToken token =
                EmailVerificationToken.create(
                        USER_ID,
                        EMAIL,
                        TOKEN,
                        CREATED_AT
                );

        assertNull(token.getId());
        assertEquals(USER_ID, token.getUserId());
        assertEquals(EMAIL, token.getUserEmail());
        assertEquals(TOKEN, token.getToken());

        assertEquals(CREATED_AT, token.getCreatedAt());
        assertEquals(
                CREATED_AT.plus(Duration.ofDays(1)),
                token.getExpiresAt()
        );

        assertNull(token.getConfirmedAt());
        assertEquals(
                EmailVerificationStatus.PENDING,
                token.getStatus()
        );
    }

    @Test
    void createShouldRejectNullUserId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> EmailVerificationToken.create(
                        null,
                        EMAIL,
                        TOKEN,
                        CREATED_AT
                )
        );
    }

    @Test
    void createShouldRejectNullOrBlankEmail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> EmailVerificationToken.create(
                        USER_ID,
                        null,
                        TOKEN,
                        CREATED_AT
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> EmailVerificationToken.create(
                        USER_ID,
                        "",
                        TOKEN,
                        CREATED_AT
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> EmailVerificationToken.create(
                        USER_ID,
                        " ",
                        TOKEN,
                        CREATED_AT
                )
        );
    }

    @Test
    void createShouldRejectNullOrBlankToken() {
        assertThrows(
                IllegalArgumentException.class,
                () -> EmailVerificationToken.create(
                        USER_ID,
                        EMAIL,
                        null,
                        CREATED_AT
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> EmailVerificationToken.create(
                        USER_ID,
                        EMAIL,
                        "",
                        CREATED_AT
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> EmailVerificationToken.create(
                        USER_ID,
                        EMAIL,
                        " ",
                        CREATED_AT
                )
        );
    }

    @Test
    void createShouldRejectNullCreatedAt() {
        assertThrows(
                IllegalArgumentException.class,
                () -> EmailVerificationToken.create(
                        USER_ID,
                        EMAIL,
                        TOKEN,
                        null
                )
        );
    }

    //############################################ Rehydration ##################################################

    @Test
    void rehydrateShouldRestorePersistedState() {
        Instant expiresAt =
                CREATED_AT.plus(Duration.ofDays(1));

        Instant confirmedAt =
                CREATED_AT.plus(Duration.ofHours(2));

        EmailVerificationToken token =
                EmailVerificationToken.rehydrate(
                        42L,
                        USER_ID,
                        EMAIL,
                        TOKEN,
                        CREATED_AT,
                        expiresAt,
                        confirmedAt,
                        EmailVerificationStatus.CONFIRMED
                );

        assertEquals(42L, token.getId());
        assertEquals(USER_ID, token.getUserId());
        assertEquals(EMAIL, token.getUserEmail());
        assertEquals(TOKEN, token.getToken());
        assertEquals(CREATED_AT, token.getCreatedAt());
        assertEquals(expiresAt, token.getExpiresAt());
        assertEquals(confirmedAt, token.getConfirmedAt());
        assertEquals(
                EmailVerificationStatus.CONFIRMED,
                token.getStatus()
        );
    }

    @Test
    void rehydrateShouldRejectNullId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> EmailVerificationToken.rehydrate(
                        null,
                        USER_ID,
                        EMAIL,
                        TOKEN,
                        CREATED_AT,
                        CREATED_AT.plus(Duration.ofDays(1)),
                        null,
                        EmailVerificationStatus.PENDING
                )
        );
    }

    @Test
    void rehydrateShouldRejectNullStatus() {
        assertThrows(
                IllegalArgumentException.class,
                () -> EmailVerificationToken.rehydrate(
                        1L,
                        USER_ID,
                        EMAIL,
                        TOKEN,
                        CREATED_AT,
                        CREATED_AT.plus(Duration.ofDays(1)),
                        null,
                        null
                )
        );
    }

    //############################################ Expiry ##################################################

    @Test
    void isExpiredAtShouldReturnFalseBeforeExpiry() {
        EmailVerificationToken token =
                createValidToken();

        assertFalse(
                token.isExpiredAt(
                        CREATED_AT.plus(Duration.ofHours(23))
                )
        );
    }

    @Test
    void isExpiredAtShouldReturnTrueExactlyAtExpiry() {
        EmailVerificationToken token =
                createValidToken();

        assertTrue(
                token.isExpiredAt(
                        CREATED_AT.plus(Duration.ofDays(1))
                )
        );
    }

    @Test
    void isExpiredAtShouldReturnTrueAfterExpiry() {
        EmailVerificationToken token =
                createValidToken();

        assertTrue(
                token.isExpiredAt(
                        CREATED_AT.plus(Duration.ofDays(2))
                )
        );
    }

    @Test
    void isExpiredAtShouldRejectNullInstant() {
        EmailVerificationToken token =
                createValidToken();

        assertThrows(
                IllegalArgumentException.class,
                () -> token.isExpiredAt(null)
        );
    }

    //############################################ Confirmation ##################################################

    @Test
    void confirmAtShouldConfirmPendingToken() {
        EmailVerificationToken token =
                createValidToken();

        Instant confirmedAt =
                CREATED_AT.plus(Duration.ofHours(1));

        token.confirmAt(confirmedAt);

        assertEquals(
                EmailVerificationStatus.CONFIRMED,
                token.getStatus()
        );

        assertEquals(
                confirmedAt,
                token.getConfirmedAt()
        );
    }

    @Test
    void confirmAtShouldRejectExpiredToken() {
        EmailVerificationToken token =
                createValidToken();

        Instant expiry =
                CREATED_AT.plus(Duration.ofDays(1));

        assertThrows(
                IllegalStateException.class,
                () -> token.confirmAt(expiry)
        );

        assertEquals(
                EmailVerificationStatus.PENDING,
                token.getStatus()
        );

        assertNull(token.getConfirmedAt());
    }

    @Test
    void confirmAtShouldRejectAlreadyConfirmedToken() {
        EmailVerificationToken token =
                createValidToken();

        Instant firstConfirmation =
                CREATED_AT.plus(Duration.ofHours(1));

        token.confirmAt(firstConfirmation);

        assertThrows(
                IllegalStateException.class,
                () -> token.confirmAt(
                        CREATED_AT.plus(Duration.ofHours(2))
                )
        );

        assertEquals(
                firstConfirmation,
                token.getConfirmedAt()
        );
    }

    @Test
    void confirmAtShouldRejectCancelledToken() {
        EmailVerificationToken token =
                createValidToken();

        token.cancelAt(
                CREATED_AT.plus(Duration.ofHours(1))
        );

        assertThrows(
                IllegalStateException.class,
                () -> token.confirmAt(
                        CREATED_AT.plus(Duration.ofHours(2))
                )
        );
    }

    //############################################ Cancellation ##################################################

    @Test
    void cancelAtShouldCancelPendingToken() {
        EmailVerificationToken token =
                createValidToken();

        token.cancelAt(
                CREATED_AT.plus(Duration.ofHours(1))
        );

        assertEquals(
                EmailVerificationStatus.CANCELLED,
                token.getStatus()
        );

        assertNull(token.getConfirmedAt());
    }

    @Test
    void cancelAtShouldRejectExpiredToken() {
        EmailVerificationToken token =
                createValidToken();

        Instant expiry =
                CREATED_AT.plus(Duration.ofDays(1));

        assertThrows(
                IllegalStateException.class,
                () -> token.cancelAt(expiry)
        );

        assertEquals(
                EmailVerificationStatus.PENDING,
                token.getStatus()
        );
    }

    @Test
    void cancelAtShouldRejectConfirmedToken() {
        EmailVerificationToken token =
                createValidToken();

        token.confirmAt(
                CREATED_AT.plus(Duration.ofHours(1))
        );

        assertThrows(
                IllegalStateException.class,
                () -> token.cancelAt(
                        CREATED_AT.plus(Duration.ofHours(2))
                )
        );

        assertEquals(
                EmailVerificationStatus.CONFIRMED,
                token.getStatus()
        );
    }

    @Test
    void cancelAtShouldRejectAlreadyCancelledToken() {
        EmailVerificationToken token =
                createValidToken();

        token.cancelAt(
                CREATED_AT.plus(Duration.ofHours(1))
        );

        assertThrows(
                IllegalStateException.class,
                () -> token.cancelAt(
                        CREATED_AT.plus(Duration.ofHours(2))
                )
        );

        assertEquals(
                EmailVerificationStatus.CANCELLED,
                token.getStatus()
        );
    }

    //############################################ Helpers ##################################################

    private EmailVerificationToken createValidToken() {
        return EmailVerificationToken.create(
                USER_ID,
                EMAIL,
                TOKEN,
                CREATED_AT
        );
    }
}

