package com.timcritt.tfg.infrastructure.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenHasherAdapterTest {

    private static final String VALID_SECRET =
            "this-is-a-valid-secret-key";

    @Test
    void constructorShouldRejectNullSecret() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TokenHasherAdapter(null)
        );
    }

    @Test
    void constructorShouldRejectSecretShorterThanMinimumLength() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TokenHasherAdapter("short-secret")
        );
    }

    @Test
    void constructorShouldAcceptSecretAtMinimumLength() {
        assertDoesNotThrow(
                () -> new TokenHasherAdapter("1234567890123456")
        );
    }

    @Test
    void hashShouldRejectNullToken() {
        TokenHasherAdapter hasher =
                new TokenHasherAdapter(VALID_SECRET);

        assertThrows(
                IllegalArgumentException.class,
                () -> hasher.hash(null)
        );
    }

    @Test
    void hashShouldRejectEmptyToken() {
        TokenHasherAdapter hasher =
                new TokenHasherAdapter(VALID_SECRET);

        assertThrows(
                IllegalArgumentException.class,
                () -> hasher.hash("")
        );
    }

    @Test
    void hashShouldRejectBlankToken() {
        TokenHasherAdapter hasher =
                new TokenHasherAdapter(VALID_SECRET);

        assertThrows(
                IllegalArgumentException.class,
                () -> hasher.hash("   ")
        );
    }

    @Test
    void hashShouldBeDeterministicForSameTokenAndSecret() {
        TokenHasherAdapter hasher =
                new TokenHasherAdapter(VALID_SECRET);

        String first =
                hasher.hash("token-123");

        String second =
                hasher.hash("token-123");

        assertEquals(first, second);
    }

    @Test
    void hashShouldProduceDifferentHashesForDifferentTokens() {
        TokenHasherAdapter hasher =
                new TokenHasherAdapter(VALID_SECRET);

        String first =
                hasher.hash("token-123");

        String second =
                hasher.hash("token-456");

        assertNotEquals(first, second);
    }

    @Test
    void hashShouldProduceDifferentHashesForDifferentSecrets() {
        TokenHasherAdapter firstHasher =
                new TokenHasherAdapter(
                        "first-valid-secret-key"
                );

        TokenHasherAdapter secondHasher =
                new TokenHasherAdapter(
                        "second-valid-secret-key"
                );

        String first =
                firstHasher.hash("same-token");

        String second =
                secondHasher.hash("same-token");

        assertNotEquals(first, second);
    }

    @Test
    void hashShouldReturnSha256SizedLowercaseHexString() {
        TokenHasherAdapter hasher =
                new TokenHasherAdapter(VALID_SECRET);

        String hash =
                hasher.hash("token-123");

        assertEquals(64, hash.length());
        assertTrue(hash.matches("[0-9a-f]{64}"));
    }

    @Test
    void hashShouldNotReturnRawToken() {
        TokenHasherAdapter hasher =
                new TokenHasherAdapter(VALID_SECRET);

        String rawToken = "token-123";

        String hash =
                hasher.hash(rawToken);

        assertNotEquals(rawToken, hash);
    }

    @Test
    void hashShouldMatchKnownHmacSha256Value() {
        TokenHasherAdapter hasher =
                new TokenHasherAdapter(
                        "1234567890123456"
                );

        String hash =
                hasher.hash("token-123");

        assertEquals(
                "14d5967748ec9929e49a07fd6f4de1fd455f718df222a0a7a7c5449af8561cba",
                hash
        );
    }
}