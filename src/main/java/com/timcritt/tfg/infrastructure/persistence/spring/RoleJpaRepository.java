package com.timcritt.tfg.infrastructure.persistence.spring;

import com.timcritt.tfg.domain.model.RoleType;
import com.timcritt.tfg.infrastructure.persistence.jpa.RoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleJpaRepository extends JpaRepository<RoleJpaEntity, Long> {
    Optional<RoleJpaEntity> findByRoleType(RoleType roleType);
}

