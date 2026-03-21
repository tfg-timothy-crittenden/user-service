package com.timcritt.tfg.infrastructure.persistence.spring;

import com.timcritt.tfg.infrastructure.persistence.jpa.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
}

