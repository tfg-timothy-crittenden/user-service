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

        Set<Role> roles = entity.getUserRoles() == null
                ? new HashSet<>()
                : entity.getUserRoles()
                .stream()
                .map(RoleEntityMapper::toDomain)
                .collect(Collectors.toSet());

        User user = new User(
                entity.getId(),
                entity.getUsername(),
                entity.getName(),
                entity.getSurname(),
                entity.getEmail(),
                roles,
                PasswordHash.of(entity.getPasswordHash())
        );
        user.setVerified(entity.isVerified());
        return user;
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