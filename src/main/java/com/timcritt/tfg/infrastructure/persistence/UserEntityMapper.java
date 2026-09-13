package com.timcritt.tfg.infrastructure.persistence;

import com.timcritt.tfg.domain.model.Role;
import com.timcritt.tfg.domain.model.aggregate.user.PasswordHash;
import com.timcritt.tfg.domain.model.aggregate.user.User;
import com.timcritt.tfg.infrastructure.persistence.jpa.UserJpaEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class UserEntityMapper {

    public static User toDomain(UserJpaEntity entity) {
        if (entity == null) return null;

        return User.rehydrate(
                entity.getId(),
                entity.getUsername(),
                entity.getName(),
                entity.getSurname(),
                entity.getEmail(),
                entity.getUserRoles(),
                PasswordHash.of(entity.getPasswordHash()),
                entity.isVerified());

    }

    public static UserJpaEntity toEntity(User domain) {
        if (domain == null) return null;

        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(domain.getId());
        entity.setUsername(domain.getUsername());
        entity.setName(domain.getName());
        entity.setSurname(domain.getSurname());
        entity.setEmail(domain.getEmail());
        entity.setPasswordHash(domain.getPasswordHash().value());
        entity.setVerified(domain.isVerified());
        entity.setUserRoles(new HashSet<>());

        return entity;
    }
}