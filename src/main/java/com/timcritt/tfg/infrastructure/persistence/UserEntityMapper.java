package com.timcritt.tfg.infrastructure.persistence.mapper;

import com.timcritt.tfg.domain.model.Role;
import com.timcritt.tfg.domain.model.User;
import com.timcritt.tfg.infrastructure.persistence.RoleEntityMapper;
import com.timcritt.tfg.infrastructure.persistence.jpa.RoleJpaEntity;
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

        return new User(
                entity.getId(),
                entity.getUsername(),
                entity.getName(),
                entity.getSurname(),
                entity.getEmail(),
                roles
        );
    }

    public static UserJpaEntity toEntity(User domain) {
        if (domain == null) return null;

        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(domain.getId());
        entity.setUsername(domain.getUsername());
        entity.setName(domain.getName());
        entity.setSurname(domain.getSurname());
        entity.setEmail(domain.getEmail());

        Set<RoleJpaEntity> roleEntities = domain.getRoles() == null
                ? new HashSet<>()
                : domain.getRoles()
                .stream()
                .map(RoleEntityMapper::toEntity)
                .collect(Collectors.toSet());

        entity.setUserRoles(roleEntities);

        return entity;
    }
}