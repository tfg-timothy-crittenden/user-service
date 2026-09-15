package com.timcritt.tfg.infrastructure.service;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UuidTokenGeneratorAdapterTest {

    @Test
    void generateShouldReturnValidUuid() {
        UuidTokenGeneratorAdapter generator = new UuidTokenGeneratorAdapter();

        String token = generator.generate();

        assertNotNull(token);
        assertDoesNotThrow(() -> UUID.fromString(token));
    }

    @Test
    void generateShouldReturnDifferentTokens() {
        UuidTokenGeneratorAdapter generator = new UuidTokenGeneratorAdapter();

        String first = generator.generate();
        String second = generator.generate();

        assertNotEquals(first, second);
    }

    @Test
    void generateShouldReturnNonBlankToken() {
        UuidTokenGeneratorAdapter generator = new UuidTokenGeneratorAdapter();

        String token = generator.generate();

        assertFalse(token.isBlank());
    }
}