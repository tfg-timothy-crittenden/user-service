package com.timcritt.tfg.domain.model.aggregate.passwordReset;

import com.timcritt.tfg.domain.exception.InvalidPasswordResetTokenException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordResetTokenTest {

    @Test
    public void createShouldThrowWhenRequiredFieldsAreNull() {
       assertThrows(IllegalArgumentException.class, () -> {PasswordResetToken.create(null, "hash", Instant.now());});
       assertThrows(IllegalArgumentException.class, () -> {PasswordResetToken.create(1L, null, Instant.now());});
       assertThrows(IllegalArgumentException.class, () -> {PasswordResetToken.create(1L, "hash", null);});
    }

    @Test
    public void createShouldThrowWhenRequiredFieldsAreEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {PasswordResetToken.create(1L, "", Instant.now());});
        assertThrows(IllegalArgumentException.class, () -> {PasswordResetToken.create(1L, " ", Instant.now());});
    }

    @Test
    public void createShouldThrowWhenExpiryAtIsBeforeCreatedAt() {
        assertThrows(IllegalArgumentException.class, () -> {PasswordResetToken.rehydrate(1L, 1L, "hash", Instant.now(), Instant.now().minus(Duration.ofSeconds(1)), true);});
    }

    @Test void createShouldSetExpireAtOneHourAheadOfCreatedAt() {
        PasswordResetToken token = PasswordResetToken.create(1L, "hash", Instant.now());
        assertNotNull(token);
        assertEquals(token.getCreatedAt().plus(Duration.ofHours(1)), token.getExpiresAt());
    }

    @Test
    public void rehydrateShouldThrowWhenRequiredFieldsAreNull() {
        assertThrows(IllegalArgumentException.class, () -> {PasswordResetToken.rehydrate(null, 1L, "hash", Instant.now(), Instant.now().plus(Duration.ofHours(1)), true);});
        assertThrows(IllegalArgumentException.class, () -> {PasswordResetToken.rehydrate(1L, null, "hash", Instant.now(), Instant.now().plus(Duration.ofHours(1)), true);});
        assertThrows(IllegalArgumentException.class, () -> {PasswordResetToken.rehydrate(1L, 1L, null, Instant.now(), Instant.now().plus(Duration.ofHours(1)), true);});
        assertThrows(IllegalArgumentException.class, () -> {PasswordResetToken.rehydrate(1L, 1L, "hash", null, Instant.now().plus(Duration.ofHours(1)), true);});
        assertThrows(IllegalArgumentException.class, () -> {PasswordResetToken.rehydrate(1L, 1L, "hash", Instant.now(), null, true);});
    }

    @Test
    public void rehydrateShouldThrowWhenRequiredFieldsAreEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {PasswordResetToken.rehydrate(1L, 1L, "", Instant.now(), Instant.now().plus(Duration.ofHours(1)), true);});
        assertThrows(IllegalArgumentException.class, () -> {PasswordResetToken.rehydrate(1L, 1L, " ", Instant.now(), Instant.now().plus(Duration.ofHours(1)), true);});
    }

    @Test
    public void isExpiredAtShouldReturnTrueWhenExpiryAtIsBeforeNow() {
        Instant pastTime = Instant.now().minus(Duration.ofHours(1));
        PasswordResetToken expiredToken = PasswordResetToken.create(1L, "hash", pastTime);
        assertTrue(expiredToken.isExpiredAt(Instant.now()));
    }

    @Test
    public void isExpiredAtShouldReturnFalseWhenNotExpired() {
        Instant createdAt = Instant.now();
        PasswordResetToken expiredToken = PasswordResetToken.create(1L, "hash", createdAt);
        assertFalse(expiredToken.isExpiredAt(Instant.now()));
    }

    @Test
    public void isUsableAtShouldReturnTrueWhenTokenValidAndNotExpired() {
        Instant createdAt = Instant.now();
        PasswordResetToken validToken = PasswordResetToken.rehydrate(1L, 1L, "hash", createdAt, createdAt.plus(Duration.ofHours(1)), true);
        assertTrue(validToken.isUsableAt(Instant.now()));
    }

    @Test
    public void isUsableAtShouldReturnFalseWhenTokenValidAndExpired() {
        Instant createdAt = Instant.now().minus(Duration.ofHours(2));
        PasswordResetToken expiredToken = PasswordResetToken.rehydrate(1L, 1L, "hash", createdAt, createdAt.plus(Duration.ofHours(1)), true);
        assertFalse(expiredToken.isUsableAt(Instant.now()));
    }

    @Test
    public void isUsableAtShouldReturnFalseWhenTokenNotValidAndNotExpired() {
        Instant createdAt = Instant.now();
        PasswordResetToken notExpiredToken = PasswordResetToken.rehydrate(1L, 1L, "hash", createdAt, createdAt.plus(Duration.ofHours(1)), false);
        assertFalse(notExpiredToken.isUsableAt(Instant.now()));
    }

    @Test
    public void consumeAtShouldThrowWhenTokenExpired() {
        PasswordResetToken expiredToken = PasswordResetToken.create(1L, "hash", Instant.now().minus(Duration.ofHours(1)));
        assertThrows(InvalidPasswordResetTokenException.class, () -> {expiredToken.consumeAt(Instant.now());});
    }

    @Test
    public void consumeAtShouldThrowWhenTokenNotValid() {
        PasswordResetToken invalidToken = PasswordResetToken.rehydrate(1L, 1L, "hash", Instant.now(), Instant.now().plus(Duration.ofHours(1)), false);
        assertThrows(InvalidPasswordResetTokenException.class, () -> invalidToken.consumeAt(Instant.now()));
    }

    @Test
    public void consumeAtShouldInvalidateToken() {
        PasswordResetToken token = PasswordResetToken.create(1L, "hash", Instant.now());
        assertTrue(token.isUsableAt(Instant.now()));
        assertTrue(token.isValid());

        token.consumeAt(Instant.now());

        assertFalse(token.isUsableAt(Instant.now()));
        assertFalse(token.isValid());
    }

    @Test
    public void revokedShouldInvalidateToken() {
        PasswordResetToken token = PasswordResetToken.create(1L, "hash", Instant.now());
        assertTrue(token.isUsableAt(Instant.now()));
        assertTrue(token.isValid());

        token.revoke();

        assertFalse(token.isUsableAt(Instant.now()));
        assertFalse(token.isValid());
    }

    @Test
    void tokenShouldBeExpiredExactlyAtExpiryTime() {
        Instant createdAt = Instant.parse("2026-09-15T12:00:00Z");

        PasswordResetToken token =
                PasswordResetToken.create(1L, "hash", createdAt);

        assertTrue(token.isExpiredAt(createdAt.plus(Duration.ofHours(1))));
    }

    @Test
    void rehydrateShouldRejectExpiryEqualToCreationTime() {
        Instant instant = Instant.parse("2026-09-15T12:00:00Z");

        assertThrows(
                IllegalArgumentException.class,
                () -> PasswordResetToken.rehydrate(
                        1L, 1L, "hash", instant, instant, true
                )
        );
    }

}
