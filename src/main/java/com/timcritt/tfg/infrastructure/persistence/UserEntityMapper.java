package com.timcritt.tfg.infrastructure.persistence;

import com.timcritt.tfg.domain.model.User;
import com.timcritt.tfg.infrastructure.persistence.jpa.UserJpaEntity;

// This class provides static methods to convert between the domain model (TestItem) and the JPA entity (TestJpaEntity).

public final class UserEntityMapper {
    private UserEntityMapper() {}

    public static User toDomain(UserJpaEntity e) {
        if (e == null) return null;
        return new User(e.getId(), e.getUsername(), e.getName(), e.getSurname(), e.getEmail());
    }

    public static UserJpaEntity toEntity(User d) {
        if (d == null) return null;
        UserJpaEntity e = new UserJpaEntity();
        e.setId(d.getId());
        e.setUsername(d.getUsername());
        e.setName(d.getName());
        e.setSurname(d.getSurname());
        e.setEmail(d.getEmail());
        return e;
    }
}

