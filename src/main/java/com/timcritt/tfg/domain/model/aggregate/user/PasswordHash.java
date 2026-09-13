package com.timcritt.tfg.domain.model.aggregate.user;

import java.util.Objects;

public final class PasswordHash {

    private final String value;

    private PasswordHash(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Password hash cannot be null or blank");
        }

        this.value = value;
    }

    public static PasswordHash of(String value) {
        return new PasswordHash(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PasswordHash that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "PasswordHash[REDACTED]";
    }
}