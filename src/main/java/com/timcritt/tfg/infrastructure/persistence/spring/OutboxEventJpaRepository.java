package com.timcritt.tfg.infrastructure.persistence.spring;

import com.timcritt.tfg.infrastructure.persistence.jpa.OutboxEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OutboxEventJpaRepository
        extends JpaRepository<OutboxEventJpaEntity, UUID> {
}