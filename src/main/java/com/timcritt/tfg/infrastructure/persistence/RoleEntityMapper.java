package com.timcritt.tfg.infrastructure.persistence;

import com.timcritt.tfg.domain.model.Role;
import com.timcritt.tfg.infrastructure.persistence.jpa.RoleJpaEntity;

public class RoleEntityMapper {

    public static Role toDomain(RoleJpaEntity entity) {
        if (entity == null) return null;

        return new Role(
                entity.getId(),
                entity.getRoleType()
        );
    }

    public static RoleJpaEntity toEntity(Role domain) {
        if (domain == null) return null;

        RoleJpaEntity entity = new RoleJpaEntity();
        entity.setId(domain.getId());
        entity.setRoleType(domain.getRoleType());
        return entity;
    }
}