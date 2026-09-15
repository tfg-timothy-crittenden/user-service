package com.timcritt.tfg.infrastructure.persistence;

import com.timcritt.tfg.domain.model.aggregate.platformInvitation.PlatformInvitation;
import com.timcritt.tfg.infrastructure.persistence.jpa.PlatformInvitationJpaEntity;

public class PlatformInvitationEntityMapper {

    static PlatformInvitation toDomain(PlatformInvitationJpaEntity jpaEntity) {
        return PlatformInvitation.rehydrate(
                jpaEntity.getId(),
                jpaEntity.getCreatedByUserId(),
                jpaEntity.getInviteeEmail(),
                jpaEntity.getToken(),
                jpaEntity.getRole(),
                jpaEntity.getCreatedAt(),
                jpaEntity.getExpiresAt(),
                jpaEntity.getConfirmedAt(),
                jpaEntity.getPlatformInvitationStatus()
        );

    }

    static PlatformInvitationJpaEntity toEntity(PlatformInvitation domain) {
        PlatformInvitationJpaEntity entity = new PlatformInvitationJpaEntity();
        entity.setId(domain.getId());
        entity.setCreatedByUserId(domain.getCreatedByUserId());
        entity.setInviteeEmail(domain.getInviteeEmail());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setConfirmedAt(domain.getConfirmedAt());
        entity.setPlatformInvitationStatus(domain.getPlatformInvitationStatus());
        entity.setToken(domain.getToken());
        entity.setRole(domain.getRole());

        return entity;

    }

}
